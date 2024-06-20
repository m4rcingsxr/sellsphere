
package com.sellsphere.client.webhook;

import com.google.gson.JsonSyntaxException;
import com.sellsphere.client.checkout.PaymentIntentRepository;
import com.sellsphere.client.checkout.TransactionService;
import com.sellsphere.client.customer.CustomerRepository;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.order.OrderService;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.common.entity.Card;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Refund;
import com.stripe.model.*;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookRestController {
    private final CardRepository cardRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final WebhookConfig webhookConfig;
    private final ExecutorService taskExecutor;
    private final OrderService orderService;
    private final StripeCheckoutService stripeService;
    private final TransactionService transactionService;

    // Webhook endpoints might occasionally receive the same event more than once
    private final ConcurrentHashMap<String, Boolean> processedEvents = new ConcurrentHashMap<>();
    private final CustomerRepository customerRepository;
    private final SettingService settingService;
    private final CustomerService customerService;
    private final RefundRepository refundRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final ChargeRepository chargeRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final CurrencyRepository currencyRepository;

    // test env - not public endpoint
    @PostMapping("/webhook")
    public ResponseEntity<String> handleCheckoutWebhook(@RequestBody String payload,
                                                        HttpServletRequest request)
            throws IOException, CustomerNotFoundException, CurrencyNotFoundException {
        String requestIp = request.getRemoteAddr();

        if (!isAllowedIp(requestIp)) {
            log.warn("Unauthorized IP address: {}", requestIp);
            return ResponseEntity.status(403).body("Forbidden");
        }


        Event event;

        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            log.warn("⚠️  Webhook error while parsing basic request.");
            return ResponseEntity.badRequest().body("");
        }

        String sigHeader = request.getHeader("Stripe-Signature");

        if (webhookConfig.getEndpointSecret() != null && sigHeader != null) {
            // Only verify the event if you have an endpoint secret defined.
            // Otherwise use the basic event deserialized with GSON.
            try {
                event = Webhook.constructEvent(
                        payload, sigHeader, webhookConfig.getEndpointSecret()
                );
            } catch (SignatureVerificationException e) {
                // Invalid signature
                log.warn("⚠️  Webhook error while validating signature.");
                return ResponseEntity.badRequest().body("");
            }
        }

        // Check if the event has already been processed
        if (processedEvents.putIfAbsent(event.getId(), Boolean.TRUE) != null) {
            log.info("Duplicate event received: {}", event.getId());
            return ResponseEntity.ok("Duplicate event ignored");
        }


        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            Optional<StripeObject> stripeObject = dataObjectDeserializer.getObject();

            String currency = settingService.getCurrencyCode(false);

            final Event finalEvent = event;
            taskExecutor.submit(() -> {
                try {
                    processEvent(stripeObject, finalEvent, currency);
                } catch (Exception e) {
                    log.error("Customer not found", e);
                    throw new RuntimeException(e);
                }
            });
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            throw new IOException(
                    "Deserialization failed, probably due to an API version mismatch");
        }


        return ResponseEntity.ok().build();
    }

    private void processEvent(Optional<StripeObject> stripeObjectOpt, Event event, String currency)
            throws CustomerNotFoundException, AddressNotFoundException, CountryNotFoundException,
            StripeException, TransactionNotFoundException, RefundNotFoundException,
            ChargeNotFoundException, CurrencyNotFoundException {

        StripeObject stripeObject;
        if (stripeObjectOpt.isPresent()) {
            stripeObject = stripeObjectOpt.get();
        } else {
            log.error("Deserialization of event data failed.");
            return;
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.partially_funded":
                // Occurs when funds are applied to a customer_balance PaymentIntent and the
                // 'amount_remaining' changes.
                break;
            case "payment_intent.processing":
                // Occurs when a PaymentIntent has started processing.
                break;
            case "payment_intent.cancelled":
                break;
            case "payment_intent.requires_action":
                // payment method - accept payment that require action to success (requires
                // action status) update service intent
                PaymentIntent stripePaymentIntent = (PaymentIntent) stripeObject;

                var servicePaymentIntent = paymentIntentRepository.findByStripeId(
                        stripePaymentIntent.getId()).orElseThrow(TransactionNotFoundException::new);

                servicePaymentIntent.setStatus(stripePaymentIntent.getStatus());
                paymentIntentRepository.save(servicePaymentIntent);
                break;

            case "payment_intent.created":

                // create service payment
                PaymentIntent stripeIntent = (PaymentIntent) stripeObject;
                Customer customer = customerRepository.findByStripeId(
                        stripeIntent.getCustomer()).orElseThrow(CustomerNotFoundException::new);
                Currency serviceCurrency = currencyRepository.findByCode(
                        stripeIntent.getCurrency()).orElseThrow(CurrencyNotFoundException::new);

                var paymentIntent = paymentIntentRepository.save(
                        com.sellsphere.common.entity.PaymentIntent.builder()
                                .applicationFeeAmount(stripeIntent.getApplicationFeeAmount())
                                .amount(stripeIntent.getAmount())
                                .created(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(1)))
                                .clientSecret(stripeIntent.getClientSecret())
                                .currency(serviceCurrency)
                                .status(stripeIntent.getStatus()) // requires_payment_method
                                .customer(customer)
                                .stripeId(stripeIntent.getId())
                                .build());

                paymentIntentRepository.save(paymentIntent);
                break;
            case "payment_intent.succeeded":
                PaymentIntent localPaymentIntent = (PaymentIntent) stripeObject;
                handlePaymentIntentSucceeded(localPaymentIntent, currency);
                break;

            case "payment_intent.failed":
                //  Occurs when a PaymentIntent has failed the attempt to create a payment method
                //  or a payment.
                break;
            case "charge.succeeded":
                Charge charge = (Charge) stripeObject;

                // create charge without balance transaction
                handleChargeSucceeded(charge);
                break;
            case "charge.pending":
                break;
            case "issuing_card.created":
                break;
            case "issuing_card.updated":
                break;
            case "charge.updated":

                // update charge with new balance transaction (same status)
                Charge updatedCharge = (Charge) stripeObject;
                String balanceTransaction = updatedCharge.getBalanceTransaction();
                BalanceTransaction balance = BalanceTransaction.retrieve(balanceTransaction);

                var serviceBalance =
                        balanceTransactionRepository.save(
                                com.sellsphere.common.entity.BalanceTransaction.builder()
                                        .fee(balance.getFee())
                                        .amount(balance.getAmount())
                                        .created(balance.getCreated())
                                        .currency(balance.getCurrency())
                                        .net(balance.getNet())
                                        .stripeId(balance.getId())
                                        .build()
                        );

                var serviceCharge = chargeRepository.findByStripeId(updatedCharge.getId())
                        .orElseThrow(ChargeNotFoundException::new);
                serviceCharge.setBalanceTransaction(serviceBalance);
                serviceCharge.setReceiptUrl(serviceCharge.getReceiptUrl());

                chargeRepository.save(serviceCharge);
                break;
            case "charge.refunded":

                // refund request (return status, and refunded)
                // update charge refunded, status, refundedAmount
                Charge chargeRefund = (Charge) stripeObject;

                var localCharge = chargeRepository
                        .findByStripeId(chargeRefund.getId())
                        .orElseThrow(ChargeNotFoundException::new);

                localCharge.setReceiptUrl(chargeRefund.getReceiptUrl());
                localCharge.setRefunded(chargeRefund.getRefunded());
                localCharge.setAmountRefunded(chargeRefund.getAmountRefunded());

                chargeRepository.save(localCharge);

                break;
            case "charge.refund.updated":

                // handle fail & status change
                Refund refund = (Refund) stripeObject;
                handleRefundUpdate(refund);
                break;
            case "payment_method.attached":

                // create service payment method and assign customer
                PaymentMethod paymentMethod = (PaymentMethod) stripeObject;
                handlePaymentMethodAttached(paymentMethod);
                break;
            case "payment_method.detached":
                break;
            case "payment_method.updated":
                break;

            case "refund.created":
                break;
            case "refund.updated":
                break;
            case "customer.created":
                break;
            case "customer.updated":
                break;
            case "customer.deleted":
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handleChargeSucceeded(Charge charge) throws TransactionNotFoundException {

        var servicePaymentIntent = paymentIntentRepository.findByStripeId(
                        charge.getPaymentIntent())
                .orElseThrow(TransactionNotFoundException::new);

        chargeRepository.save(
                com.sellsphere.common.entity.Charge.builder()
                        .stripeId(charge.getId())
                        .amount(charge.getAmount())
                        .amountRefunded(charge.getAmountRefunded())
                        .paymentIntent(servicePaymentIntent)
                        .refunded(charge.getRefunded())
                        .receiptUrl(charge.getReceiptUrl())
                        .status(charge.getStatus())
                        .build()
        );

    }

    private boolean isAllowedIp(String ip) {
        return webhookConfig.getAllowedIps().contains(ip);
    }

    private void handleRefundUpdate(Refund stripeRefund)
            throws RefundNotFoundException, StripeException {
        // status & failure reason // create refund invoke this and destination details are updated (card - reference)
        String status = stripeRefund.getStatus();
        String failureReason = stripeRefund.getFailureReason();

        BalanceTransaction newBalanceTr = BalanceTransaction.retrieve(
                stripeRefund.getBalanceTransaction());

        var serviceBalanceTr = balanceTransactionRepository.save(
                com.sellsphere.common.entity.BalanceTransaction.builder()
                        .fee(newBalanceTr.getFee())
                        .amount(newBalanceTr.getAmount())
                        .created(newBalanceTr.getCreated())
                        .currency(newBalanceTr.getCurrency())
                        .net(newBalanceTr.getNet())
                        .stripeId(newBalanceTr.getId())
                        .build()
        );

        var ref = refundRepository
                .findByStripeId(stripeRefund.getId())
                .orElseThrow(RefundNotFoundException::new);
        ref.setStatus(status);
        ref.setBalanceTransaction(serviceBalanceTr);
        ref.setFailureReason(failureReason);
        refundRepository.save(ref);
    }


    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent,
                                              String currency)
            throws CustomerNotFoundException, AddressNotFoundException, CountryNotFoundException,
            StripeException, TransactionNotFoundException {

        ShippingDetails shipping = paymentIntent.getShipping();
        String courierId = paymentIntent.getMetadata().get("courier_id");
        String customerEmail = paymentIntent.getMetadata().get("email");
        String addressIdx = paymentIntent.getMetadata().get("addressIdx");

        Customer customer = customerService.getByEmail(customerEmail);
        var tr = paymentIntentRepository.findByStripeId(paymentIntent.getId()).orElseThrow(TransactionNotFoundException::new);

        Order order = orderService.createOrder(customerEmail,
                                               Address.builder()
                                                       .line1(shipping.getAddress().getLine1())
                                                       .line2(shipping.getAddress().getLine2())
                                                       .city(shipping.getAddress().getCity())
                                                       .state(shipping.getAddress().getState())
                                                       .countryAlpha2(
                                                               shipping.getAddress().getCountry())
                                                       .contactName(shipping.getName())
                                                       .postalCode(
                                                               shipping.getAddress().getPostalCode())
                                                       // send always to customer, not recipient
                                                       .contactEmail(customerEmail)
                                                       .contactPhone(shipping.getPhone())
                                                       .build()
                , courierId, currency, addressIdx
        );

        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
        var servicePaymentMethod = paymentMethodRepository.save(
                com.sellsphere.common.entity.PaymentMethod.builder()
                        .type(paymentMethod.getType())
                        .customer(customer)
                        .stripeId(paymentMethod.getId())
                        .build()
        );

        tr.setPaymentMethod(servicePaymentMethod);
        tr.setStatus(paymentIntent.getStatus());
        tr.setOrder(order);
        transactionService.save(tr);

        stripeService.createTransaction(paymentIntent);

        log.info("Handled payment intent succeeded for: {}", paymentIntent.getId());
    }

    private void handlePaymentMethodAttached(PaymentMethod paymentMethod)
            throws CustomerNotFoundException {
        Customer customer = customerRepository
                .findByStripeId(paymentMethod.getCustomer())
                .orElseThrow(CustomerNotFoundException::new);

        // check if type is a card
        if (paymentMethod.getType().equals("card")) {
            PaymentMethod.Card card = paymentMethod.getCard();
            cardRepository.save(
                    Card.builder()
                            .brand(card.getBrand())
                            .country(card.getCountry())
                            .customer(customer)
                            .last4(card.getLast4())
                            .created(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(1)))
                            .expYear(card.getExpYear())
                            .expMonth(card.getExpMonth())
                            .funding(card.getFunding())
                            .build()
            );
        }

        log.info("Handled payment method attached for: {}", paymentMethod.getId());
    }

}

