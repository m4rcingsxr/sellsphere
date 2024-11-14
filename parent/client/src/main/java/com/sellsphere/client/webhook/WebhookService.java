package com.sellsphere.client.webhook;

import com.sellsphere.client.EmailService;
import com.sellsphere.client.checkout.CurrencyService;
import com.sellsphere.client.checkout.TransactionService;
import com.sellsphere.client.customer.CustomerRepository;
import com.sellsphere.client.order.OrderRepository;
import com.sellsphere.client.order.OrderService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.inject.Custom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    @Getter
    private final SettingService settingService;
    private final OrderService orderService;
    private final TransactionService transactionService;
    private final CurrencyService currencyService;
    private final ShoppingCartService shoppingCartService;

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final RefundRepository refundRepository;
    private final ChargeRepository chargeRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    private final ExecutorService executorService;

    /**
     * Process the Stripe event.
     *
     * @param stripeObjectOpt The Stripe object.
     * @param event           The Stripe event.
     * @throws CustomerNotFoundException    if the customer is not found.
     * @throws AddressNotFoundException     if the address is not found.
     * @throws CountryNotFoundException     if the country is not found.
     * @throws StripeException              if there is an error with Stripe operations.
     * @throws TransactionNotFoundException if the transaction is not found.
     * @throws RefundNotFoundException      if the refund is not found.
     * @throws ChargeNotFoundException      if the charge is not found.
     * @throws CurrencyNotFoundException    if the currency is not found.
     */
    public void processEvent(Optional<StripeObject> stripeObjectOpt, Event event)
            throws CustomerNotFoundException,
            StripeException, TransactionNotFoundException, RefundNotFoundException,
            ChargeNotFoundException, CurrencyNotFoundException, CountryNotFoundException {

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
                handlePaymentIntentSucceeded((com.stripe.model.PaymentIntent) stripeObject);
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
                handleChargeSucceeded((com.stripe.model.Charge) stripeObject);
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
                handlePaymentMethodDetached((com.stripe.model.PaymentMethod) stripeObject);
                break;
            case "payment_method.updated":
                break;
            case "refund.created":
                // identify whether this charge is created (stripe dashboard, admin dashboard)
                handleRefundCreated((com.stripe.model.Refund) stripeObject);
                break;
            case "refund.updated":
                break;
            case "customer.created":
                handleCustomerCreated((com.stripe.model.Customer) stripeObject);
                break;
            case "customer.updated":
                break;
            case "customer.deleted":
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCustomerCreated(com.stripe.model.Customer stripeObject) {
        customerRepository.findByEmail(stripeObject.getEmail()).ifPresent(customer -> {
            customer.setStripeId(stripeObject.getId());
            customerRepository.save(customer);
        });
    }

    protected void handlePaymentMethodDetached(com.stripe.model.PaymentMethod stripePaymentMethod) {
        if(stripePaymentMethod.getType().equals("card")) {
            Card card = cardRepository.findByStripeId(stripePaymentMethod.getId()).orElseThrow();
            cardRepository.delete(card);
        }
    }

    private void handleRefundCreated(com.stripe.model.Refund stripeRefund)
            throws TransactionNotFoundException {
        Optional<Refund> refundOpt = refundRepository.findByStripeId(stripeRefund.getId());
    }

    /**
     * Handles the payment_intent.requires_action event.
     *
     * @param stripePaymentIntent The Stripe PaymentIntent.
     * @throws TransactionNotFoundException if the transaction is not found.
     */
    private void handlePaymentIntentRequiresAction(
            com.stripe.model.PaymentIntent stripePaymentIntent)
            throws TransactionNotFoundException {

        var serviceTransaction = transactionService.getByStripeId(stripePaymentIntent.getId());

        serviceTransaction.setStatus(stripePaymentIntent.getStatus());
        transactionService.save(serviceTransaction);
    }

    /**
     * Handles the payment_intent.succeeded event.
     *
     * @param stripePaymentIntent The Stripe PaymentIntent.
     * @throws CountryNotFoundException     if the country is not found.
     * @throws StripeException              if there is an error with Stripe operations.
     * @throws TransactionNotFoundException if the transaction is not found.
     */

    private void handlePaymentIntentSucceeded(com.stripe.model.PaymentIntent stripePaymentIntent)
            throws TransactionNotFoundException, StripeException {
        CompletableFuture.runAsync(() -> {
            try {
                var serviceTransaction = transactionService.getByStripeId(stripePaymentIntent.getId());
                Customer customer = serviceTransaction.getCustomer();

                var paymentMethod = com.stripe.model.PaymentMethod.retrieve(stripePaymentIntent.getPaymentMethod());

                var servicePaymentMethod = paymentMethodRepository.save(
                        com.sellsphere.common.entity.PaymentMethod.builder()
                                .type(paymentMethod.getType())
                                .customer(customer)
                                .stripeId(paymentMethod.getId())
                                .build()
                );

                serviceTransaction.setPaymentMethod(servicePaymentMethod);
                serviceTransaction.setStatus(stripePaymentIntent.getStatus());
                serviceTransaction.setReceiptUrl(
                        com.stripe.model.Charge.retrieve(stripePaymentIntent.getLatestCharge()).getReceiptUrl());
                transactionService.save(serviceTransaction);

                serviceTransaction.getOrder().addOrderTrack(
                        OrderTrack.builder()
                                .status(OrderStatus.PAID)
                                .notes(OrderStatus.PAID.getNote())
                                .updatedTime(LocalDate.now())
                                .order(serviceTransaction.getOrder())
                                .build()
                );

                Order order = orderRepository.save(serviceTransaction.getOrder());

                // Send confirmation email
                emailService.sendOrderConfirmationEmail(order);

                // Clear cart
                shoppingCartService.clearCart(customer);

                log.info("Handled payment intent succeeded for: {}", stripePaymentIntent.getId());
            } catch (Exception e) {
                log.error("Error handling payment intent succeeded for: {}", stripePaymentIntent.getId(), e);
            }
        }, executorService);
    }


    /**
     * Handles the payment_intent.created event.
     *
     * @param stripeIntent The Stripe PaymentIntent.
     */
    private void handlePaymentIntentCreated(com.stripe.model.PaymentIntent stripeIntent) {
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
    private void handleChargeUpdated(com.stripe.model.Charge updatedCharge)
            throws ChargeNotFoundException, StripeException, CurrencyNotFoundException {
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
    private void handleRefundUpdate(com.stripe.model.Refund stripeRefund)
            throws RefundNotFoundException, StripeException, CurrencyNotFoundException {
        String status = stripeRefund.getStatus();
        String failureReason = stripeRefund.getFailureReason();

        var balanceTransaction = com.stripe.model.BalanceTransaction.retrieve(
                stripeRefund.getBalanceTransaction());

        var serviceBalanceTr = createServiceBalanceTransaction(balanceTransaction);

        var ref = refundRepository.findByStripeId(stripeRefund.getId())
                .orElseThrow(RefundNotFoundException::new);
        ref.setStatus(status);
        ref.setBalanceTransaction(serviceBalanceTr);
        ref.setFailureReason(failureReason);
        refundRepository.save(ref);
    }

    private BalanceTransaction createServiceBalanceTransaction(
            com.stripe.model.BalanceTransaction balanceTransaction)
            throws CurrencyNotFoundException {
        Currency currency = currencyService.getByCode(balanceTransaction.getCurrency());

        return balanceTransactionRepository.save(
                com.sellsphere.common.entity.BalanceTransaction.builder()
                        .fee(balanceTransaction.getFee())
                        .amount(balanceTransaction.getAmount())
                        .created(balanceTransaction.getCreated())
                        .currency(currency)
                        .net(balanceTransaction.getNet())
                        .exchangeRate(balanceTransaction.getExchangeRate())
                        .stripeId(balanceTransaction.getId())
                        .build());
    }

    /**
     * Handles the charge.succeeded event.
     *
     * @param charge The Stripe Charge.
     * @throws TransactionNotFoundException if the transaction is not found.
     */
    private void handleChargeSucceeded(com.stripe.model.Charge charge)
            throws TransactionNotFoundException {
        var servicePaymentIntent = transactionService.getByStripeId(charge.getPaymentIntent());

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
    private void handlePaymentMethodAttached(com.stripe.model.PaymentMethod paymentMethod)
            throws CustomerNotFoundException {
        Customer customer = customerRepository
                .findByStripeId(paymentMethod.getCustomer())
                .orElseThrow(CustomerNotFoundException::new);


        var card = paymentMethod.getCard();
        cardRepository.save(
                Card.builder()
                        .stripeId(paymentMethod.getId())
                        .brand(card.getBrand())
                        .country(card.getCountry())
                        .customer(customer)
                        .last4(card.getLast4())
                        .created(Instant.now().toEpochMilli())
                        .expYear(card.getExpYear())
                        .expMonth(card.getExpMonth())
                        .funding(card.getFunding())
                        .build());

        log.info("Handled payment method attached for: {}", paymentMethod.getId());
    }

}
