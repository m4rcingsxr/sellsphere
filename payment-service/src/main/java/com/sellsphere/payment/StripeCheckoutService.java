package com.sellsphere.payment;

import com.sellsphere.common.entity.payload.PaymentRequestDTO;
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

/**
 * Service for handling Stripe checkout operations.
 * This class provides methods to create payment intents, refunds, transactions,
 * and customer sessions, as well as perform tax calculations.
 */
public class StripeCheckoutService {

    static {
        StripeConfig.init();
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

        return PaymentIntent.create(params);
    }

    /**
     * Updates a Stripe payment intent with the given request details.
     *
     * @param paymentIntentId The ID of the payment intent to update.
     * @param request         The payment request details.
     * @return The updated PaymentIntent object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public PaymentIntent updatePaymentIntent(String paymentIntentId, PaymentRequestDTO request
    ) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        var builder = PaymentIntentUpdateParams.builder()
                .setAmount(request.getAmountTotal())
                .setCurrency(request.getCurrencyCode());

        if (request.getAddress() != null) {
            builder.setShipping(
                    PaymentIntentUpdateParams.Shipping.builder()
                            .setPhone(request.getPhoneNumber())
                            .setName(request.getAddress().getFullName())
                            .setAddress(
                                    PaymentIntentUpdateParams.Shipping.Address.builder()
                                            .setCity(request.getAddress().getCity())
                                            .setState(request.getAddress().getState())
                                            .setLine1(
                                                    request.getAddress().getAddressLine1())
                                            .setLine2(
                                                    request.getAddress().getAddressLine2())
                                            .setCountry(
                                                    request.getAddress().getCountryCode())
                                            .setPostalCode(
                                                    request.getAddress().getPostalCode())
                                            .build())
                            .build()
            );
        }

        return paymentIntent.update(builder.build());
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


        RefundCreateParams params = RefundCreateParams.builder()
                .setAmount(amount)
                .setPaymentIntent(paymentIntent)
                .setReason(reason)
                .build();

        return Refund.create(params);
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
        // crete transaction, use payment intent id as reference
        var transactionParams = TransactionCreateFromCalculationParams.builder()
                .setCalculation(paymentIntent.getMetadata().get("calculation_id"))
                .setReference(paymentIntent.getId())
                .addExpand("line_items")
                .build();

        Transaction transaction = Transaction.createFromCalculation(transactionParams);

        // store transaction id in payment metadata so that later you can record refunds
        PaymentIntentUpdateParams paymentParams =
                PaymentIntentUpdateParams.builder()
                        .putMetadata("tax_transaction", transaction.getId())
                        .build();

        paymentIntent.update(paymentParams);
        return transaction;
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
        return Calculation.create(params);
    }

    /**
     * Creates a customer session in Stripe.
     *
     * @param stripeId The Stripe customer ID.
     * @return The created CustomerSession object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public CustomerSession createCustomerSession(String stripeId) throws StripeException {
        CustomerSessionCreateParams csParams = CustomerSessionCreateParams.builder()
                .setCustomer(stripeId)
                .setComponents(CustomerSessionCreateParams.Components.builder().build())
                .putExtraParam("components[payment_element][enabled]", true)
                .putExtraParam(
                        "components[payment_element][features][payment_method_redisplay]",
                        "enabled"
                )
                .putExtraParam(
                        "components[payment_element][features][payment_method_save]",
                        "enabled"
                )
                .putExtraParam(
                        "components[payment_element][features][payment_method_save_usage]",
                        "on_session"
                )
                .putExtraParam(
                        "components[payment_element][features][payment_method_remove]",
                        "enabled"
                )
                .build();

        return CustomerSession.create(csParams);
    }
}
