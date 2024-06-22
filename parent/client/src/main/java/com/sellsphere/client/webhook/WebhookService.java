package com.sellsphere.client.webhook;

import com.sellsphere.client.checkout.PaymentIntentRepository;
import com.sellsphere.client.checkout.TransactionService;
import com.sellsphere.client.customer.CustomerRepository;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.order.OrderService;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.ShippingDetails;
import com.stripe.model.StripeObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    @Getter
    private final SettingService settingService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final TransactionService transactionService;
    private final StripeCheckoutService stripeService;

    private final CardRepository cardRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CustomerRepository customerRepository;
    private final RefundRepository refundRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final ChargeRepository chargeRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final CurrencyRepository currencyRepository;

    /**
     * Process the Stripe event.
     *
     * @param stripeObjectOpt The Stripe object.
     * @param event           The Stripe event.
     * @param currency        The currency code.
     * @throws CustomerNotFoundException    if the customer is not found.
     * @throws AddressNotFoundException     if the address is not found.
     * @throws CountryNotFoundException     if the country is not found.
     * @throws StripeException              if there is an error with Stripe operations.
     * @throws TransactionNotFoundException if the transaction is not found.
     * @throws RefundNotFoundException      if the refund is not found.
     * @throws ChargeNotFoundException      if the charge is not found.
     * @throws CurrencyNotFoundException    if the currency is not found.
     */
    public void processEvent(Optional<StripeObject> stripeObjectOpt, Event event, String currency)
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
                handlePaymentIntentRequiresAction((com.stripe.model.PaymentIntent) stripeObject);
                break;
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded((com.stripe.model.PaymentIntent) stripeObject, currency);
                break;

            case "payment_intent.created":
                handlePaymentIntentCreated((com.stripe.model.PaymentIntent) stripeObject);
                break;

            case "payment_intent.failed":

                //  Occurs when a PaymentIntent has failed the attempt to create a payment method
                //  or a payment.
                break;
            case "charge.succeeded":

                // create charge without balance transaction
                handleChargeSucceeded( (com.stripe.model.Charge) stripeObject);
                break;
            case "charge.pending":
                break;
            case "issuing_card.created":
                break;
            case "issuing_card.updated":
                break;
            case "charge.updated":
                handleChargeUpdated((com.stripe.model.Charge) stripeObject);
                break;
            case "charge.refunded":

                // refund request (return status, and refunded)
                // update charge refunded, status, refundedAmount
                handleChargeRefunded((com.stripe.model.Charge) stripeObject);
                break;
            case "charge.refund.updated":

                // handle fail & status change
                handleRefundUpdate((com.stripe.model.Refund) stripeObject);
                break;
            case "payment_method.attached":

                // create service payment method and assign customer
                handlePaymentMethodAttached((com.stripe.model.PaymentMethod) stripeObject);
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

    /**
     * Handles the payment_intent.requires_action event.
     *
     * @param stripePaymentIntent The Stripe PaymentIntent.
     * @throws TransactionNotFoundException if the transaction is not found.
     */
    private void handlePaymentIntentRequiresAction(com.stripe.model.PaymentIntent  stripePaymentIntent)
            throws TransactionNotFoundException {
        var servicePaymentIntent = paymentIntentRepository.findByStripeId(stripePaymentIntent.getId())
                .orElseThrow(TransactionNotFoundException::new);
        servicePaymentIntent.setStatus(stripePaymentIntent.getStatus());
        paymentIntentRepository.save(servicePaymentIntent);
    }

    /**
     * Handles the payment_intent.succeeded event.
     *
     * @param paymentIntent The Stripe PaymentIntent.
     * @param currency      The currency code.
     * @throws CustomerNotFoundException    if the customer is not found.
     * @throws AddressNotFoundException     if the address is not found.
     * @throws CountryNotFoundException     if the country is not found.
     * @throws StripeException              if there is an error with Stripe operations.
     * @throws TransactionNotFoundException if the transaction is not found.
     */
    private void handlePaymentIntentSucceeded(com.stripe.model.PaymentIntent paymentIntent, String currency)
            throws CustomerNotFoundException, AddressNotFoundException, CountryNotFoundException,
            StripeException, TransactionNotFoundException {

        ShippingDetails shipping = paymentIntent.getShipping();
        String courierId = paymentIntent.getMetadata().get("courier_id");
        String customerEmail = paymentIntent.getMetadata().get("email");
        String addressIdx = paymentIntent.getMetadata().get("addressIdx");

        Customer customer = customerService.getByEmail(customerEmail);
        var tr = paymentIntentRepository.findByStripeId(paymentIntent.getId())
                .orElseThrow(TransactionNotFoundException::new);

        Order order = orderService.createOrder(customerEmail,
                                               Address.builder()
                                                       .line1(shipping.getAddress().getLine1())
                                                       .line2(shipping.getAddress().getLine2())
                                                       .city(shipping.getAddress().getCity())
                                                       .state(shipping.getAddress().getState())
                                                       .countryAlpha2(shipping.getAddress().getCountry())
                                                       .contactName(shipping.getName())
                                                       .postalCode(shipping.getAddress().getPostalCode())
                                                       .contactEmail(customerEmail)
                                                       .contactPhone(shipping.getPhone())
                                                       .build(), courierId, currency, addressIdx);

        var paymentMethod = com.stripe.model.PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
        var servicePaymentMethod = paymentMethodRepository.save(
                com.sellsphere.common.entity.PaymentMethod.builder()
                        .type(paymentMethod.getType())
                        .customer(customer)
                        .stripeId(paymentMethod.getId())
                        .build());

        tr.setPaymentMethod(servicePaymentMethod);
        tr.setStatus(paymentIntent.getStatus());
        tr.setOrder(order);
        transactionService.save(tr);

        stripeService.createTransaction(paymentIntent);

        log.info("Handled payment intent succeeded for: {}", paymentIntent.getId());
    }

    /**
     * Handles the payment_intent.created event.
     *
     * @param stripeIntent The Stripe PaymentIntent.
     * @throws CustomerNotFoundException    if the customer is not found.
     * @throws CurrencyNotFoundException    if the currency is not found.
     */
    private void handlePaymentIntentCreated(com.stripe.model.PaymentIntent stripeIntent)
            throws CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = customerRepository.findByStripeId(stripeIntent.getCustomer())
                .orElseThrow(CustomerNotFoundException::new);
        Currency serviceCurrency = currencyRepository.findByCode(stripeIntent.getCurrency())
                .orElseThrow(CurrencyNotFoundException::new);

        var paymentIntent = paymentIntentRepository.save(
                com.sellsphere.common.entity.PaymentIntent.builder()
                        .applicationFeeAmount(stripeIntent.getApplicationFeeAmount())
                        .amount(stripeIntent.getAmount())
                        .created(Instant.now().toEpochMilli())
                        .clientSecret(stripeIntent.getClientSecret())
                        .currency(serviceCurrency)
                        .status(stripeIntent.getStatus())
                        .customer(customer)
                        .stripeId(stripeIntent.getId())
                        .build());

        paymentIntentRepository.save(paymentIntent);
    }

    private void handleChargeRefunded(com.stripe.model.Charge chargeRefund)
            throws ChargeNotFoundException {
        var localCharge = chargeRepository
                .findByStripeId(chargeRefund.getId())
                .orElseThrow(ChargeNotFoundException::new);

        localCharge.setReceiptUrl(chargeRefund.getReceiptUrl());
        localCharge.setRefunded(chargeRefund.getRefunded());
        localCharge.setAmountRefunded(chargeRefund.getAmountRefunded());

        chargeRepository.save(localCharge);
    }

    /**
     * Handles the charge.updated event.
     *
     * @param updatedCharge The Stripe Charge.
     * @throws ChargeNotFoundException if the charge is not found.
     * @throws StripeException         if there is an error with Stripe operations.
     */
    private void handleChargeUpdated(com.stripe.model.Charge updatedCharge) throws ChargeNotFoundException, StripeException {
        String balanceTransaction = updatedCharge.getBalanceTransaction();
        var balance = com.stripe.model.BalanceTransaction.retrieve(balanceTransaction);

        var serviceBalance = createServiceBalanceTransaction(balance);

        var serviceCharge = chargeRepository.findByStripeId(updatedCharge.getId())
                .orElseThrow(ChargeNotFoundException::new);
        serviceCharge.setBalanceTransaction(serviceBalance);
        serviceCharge.setReceiptUrl(serviceCharge.getReceiptUrl());

        chargeRepository.save(serviceCharge);
    }

    /**
     * Handles the charge.refund.updated event.
     *
     * @param stripeRefund The Stripe Refund.
     * @throws RefundNotFoundException if the refund is not found.
     * @throws StripeException         if there is an error with Stripe operations.
     */
    private void handleRefundUpdate(com.stripe.model.Refund stripeRefund) throws RefundNotFoundException, StripeException {
        String status = stripeRefund.getStatus();
        String failureReason = stripeRefund.getFailureReason();

        var balanceTransaction = com.stripe.model.BalanceTransaction.retrieve(stripeRefund.getBalanceTransaction());

        var serviceBalanceTr = createServiceBalanceTransaction(balanceTransaction);

        var ref = refundRepository.findByStripeId(stripeRefund.getId())
                .orElseThrow(RefundNotFoundException::new);
        ref.setStatus(status);
        ref.setBalanceTransaction(serviceBalanceTr);
        ref.setFailureReason(failureReason);
        refundRepository.save(ref);
    }

    private BalanceTransaction createServiceBalanceTransaction(com.stripe.model.BalanceTransaction balanceTransaction) {
        return balanceTransactionRepository.save(
                com.sellsphere.common.entity.BalanceTransaction.builder()
                        .fee(balanceTransaction.getFee())
                        .amount(balanceTransaction.getAmount())
                        .created(balanceTransaction.getCreated())
                        .currency(balanceTransaction.getCurrency())
                        .net(balanceTransaction.getNet())
                        .stripeId(balanceTransaction.getId())
                        .build());
    }

    /**
     * Handles the charge.succeeded event.
     *
     * @param charge The Stripe Charge.
     * @throws TransactionNotFoundException if the transaction is not found.
     */
    private void handleChargeSucceeded(com.stripe.model.Charge charge) throws TransactionNotFoundException {
        var servicePaymentIntent = paymentIntentRepository.findByStripeId(charge.getPaymentIntent())
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
                        .build());
    }


    /**
     * Handles the payment_method.attached event.
     *
     * @param paymentMethod The Stripe PaymentMethod.
     * @throws CustomerNotFoundException if the customer is not found.
     */
    private void handlePaymentMethodAttached(com.stripe.model.PaymentMethod paymentMethod) throws CustomerNotFoundException {
        Customer customer = customerRepository.findByStripeId(paymentMethod.getCustomer())
                .orElseThrow(CustomerNotFoundException::new);

        if (paymentMethod.getType().equals("card")) {
            var card = paymentMethod.getCard();
            cardRepository.save(
                    Card.builder()
                            .brand(card.getBrand())
                            .country(card.getCountry())
                            .customer(customer)
                            .last4(card.getLast4())
                            .created(Instant.now().toEpochMilli())
                            .expYear(card.getExpYear())
                            .expMonth(card.getExpMonth())
                            .funding(card.getFunding())
                            .build());
        }

        log.info("Handled payment method attached for: {}", paymentMethod.getId());
    }

}
