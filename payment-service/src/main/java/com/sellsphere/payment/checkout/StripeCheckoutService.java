package com.sellsphere.payment.checkout;

import com.google.inject.Inject;
import com.sellsphere.payment.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.CustomerSession;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.tax.Calculation;
import com.stripe.model.tax.Transaction;
import com.stripe.param.CustomerSessionCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.tax.CalculationCreateParams;
import com.stripe.param.tax.TransactionCreateFromCalculationParams;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling Stripe checkout operations.
 */
@Slf4j
public class StripeCheckoutService {

    private final StripeConfig stripeConfig;

    @Inject
    public StripeCheckoutService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    /**
     * Creates a Stripe payment intent.
     *
     * @param params The payment intent creation parameters.
     * @return The created PaymentIntent object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public PaymentIntent createPaymentIntent(PaymentIntentCreateParams params)
            throws StripeException {
        try {
            log.info("Creating payment intent");
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            log.error("Error creating payment intent", e);
            throw e;
        }
    }

    /**
     * Creates a Stripe refund for a payment intent.
     *
     * @param paymentIntent The ID of the payment intent to refund.
     * @param amount        The amount to refund.
     * @param reason        The reason for the refund.
     * @return The created Refund object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public Refund createRefund(String paymentIntent, Long amount, RefundCreateParams.Reason reason)
            throws StripeException {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setAmount(amount)
                    .setPaymentIntent(paymentIntent)
                    .setReason(reason)
                    .build();

            log.info("Creating refund for payment intent: {}", paymentIntent);
            return Refund.create(params);
        } catch (StripeException e) {
            log.error("Error creating refund", e);
            throw e;
        }
    }

    /**
     * Creates a Stripe tax transaction based on a payment intent.
     *
     * @param paymentIntent The PaymentIntent object.
     * @return The created Transaction object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public Transaction createTransaction(PaymentIntent paymentIntent)
            throws StripeException {
        try {
            var transactionParams = TransactionCreateFromCalculationParams.builder()
                    .setCalculation(paymentIntent.getMetadata().get("calculation_id"))
                    .setReference(paymentIntent.getId())
                    .addExpand("line_items")
                    .build();

            log.info("Creating tax transaction for payment intent: {}", paymentIntent.getId());
            Transaction transaction = Transaction.createFromCalculation(transactionParams);

            // Store transaction id in payment metadata
            PaymentIntentUpdateParams paymentParams = PaymentIntentUpdateParams.builder()
                    .putMetadata("tax_transaction", transaction.getId())
                    .build();

            paymentIntent.update(paymentParams);
            return transaction;
        } catch (StripeException e) {
            log.error("Error creating transaction", e);
            throw e;
        }
    }

    /**
     * Performs a tax calculation based on the given parameters.
     *
     * @param params The tax calculation parameters.
     * @return The created Calculation object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public Calculation calculate(CalculationCreateParams params)
            throws StripeException {
        try {
            log.info("Performing tax calculation");
            return Calculation.create(params);
        } catch (StripeException e) {
            log.error("Error performing tax calculation", e);
            throw e;
        }
    }

    /**
     * Creates a customer session in Stripe.
     *
     * @param stripeId The Stripe customer ID.
     * @return The created CustomerSession object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public CustomerSession createCustomerSession(String stripeId) throws StripeException {
        try {
            CustomerSessionCreateParams csParams = CustomerSessionCreateParams.builder()
                    .setCustomer(stripeId)
                    .setComponents(CustomerSessionCreateParams.Components.builder().build())
                    .putExtraParam("components[payment_element][enabled]", true)
                    .putExtraParam("components[payment_element][features][payment_method_redisplay]", "enabled")
                    .putExtraParam("components[payment_element][features][payment_method_save]", "enabled")
                    .putExtraParam("components[payment_element][features][payment_method_save_usage]", "off_session") // Allow usage off-session for reuse
                    .putExtraParam("components[payment_element][features][payment_method_remove]", "enabled")
                    .build();

            log.info("Creating customer session for Stripe ID: {}", stripeId);
            return CustomerSession.create(csParams);
        } catch (StripeException e) {
            log.error("Error creating customer session", e);
            throw e;
        }
    }
}
