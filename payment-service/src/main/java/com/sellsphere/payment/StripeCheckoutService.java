package com.sellsphere.payment;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.CustomerSession;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Calculation;
import com.stripe.model.tax.Transaction;
import com.stripe.param.CustomerSessionCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.tax.CalculationCreateParams;
import com.stripe.param.tax.TransactionCreateFromCalculationParams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * customer_tax_location_invalid error code if your customer’s address is invalid or isn’t
 * precise enough to calculate tax:
 * {
 * "error": {
 * "doc_url": "https://docs.stripe.com/error-codes#customer-tax-location-invalid",
 * "code": "customer_tax_location_invalid",
 * "message": "We could not determine the customer's tax location based on the provided customer
 * address.",
 * "param": "customer_details[address]",
 * "type": "invalid_request_error"
 * }
 * }
 */

/**
 * Service for handling Stripe checkout operations.
 * This class provides methods to create checkout sessions, payment intents,
 * and perform calculations for cart items and shipping costs.
 */
public class StripeCheckoutService {

    static {
        StripeConfig.init();
    }

    private static final String DOMAIN = "http://localhost:8081/SellSphere";

    /**
     * Creates a new checkout session with the specified cart items and supported countries.
     * This method serves for creating a session for embedded checkout elements.
     *
     * @param cart               List of cart items.
     * @param supportedCountries List of supported countries.
     * @return A Stripe Session object.
     * @throws StripeException If an error occurs while creating the session.
     */
    public Session createCheckoutSession(List<CartItem> cart, List<Country> supportedCountries)
            throws StripeException {
        SessionCreateParams.Builder builder = initializeSessionBuilder();
        addAllowedCountriesToBuilder(builder, supportedCountries);
        addCartItemsToBuilder(builder, cart);
        SessionCreateParams params = builder.build();
        return Session.create(params);
    }

    /**
     * Initializes the session builder with common parameters.
     *
     * @return The initialized session builder.
     */
    private SessionCreateParams.Builder initializeSessionBuilder() {
        return SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setAllowPromotionCodes(true)
                .setSubmitType(SessionCreateParams.SubmitType.PAY)
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                .setReturnUrl(DOMAIN + "/checkout/return?session_id={CHECKOUT_SESSION_ID}")
                .setAutomaticTax(
                        SessionCreateParams.AutomaticTax.builder()
                                .setEnabled(true)
                                .build()
                )
                .setConsentCollection(
                        SessionCreateParams.ConsentCollection.builder()
                                .setTermsOfService(
                                        SessionCreateParams.ConsentCollection.TermsOfService.REQUIRED)
                                .build()
                );
    }

    /**
     * Adds the allowed countries to the session builder.
     *
     * @param builder            The session builder.
     * @param supportedCountries List of supported countries.
     */
    private void addAllowedCountriesToBuilder(SessionCreateParams.Builder builder,
                                              List<Country> supportedCountries) {
        List<SessionCreateParams.ShippingAddressCollection.AllowedCountry> allowedCountries =
                new ArrayList<>();
        for (Country supportedCountry : supportedCountries) {
            allowedCountries.add(
                    SessionCreateParams.ShippingAddressCollection.AllowedCountry.valueOf(
                            supportedCountry.getCode())
            );
        }
        builder.setShippingAddressCollection(SessionCreateParams.ShippingAddressCollection.builder()
                                                     .addAllAllowedCountry(allowedCountries)
                                                     .build());
    }

    /**
     * Adds the cart items to the session builder.
     *
     * @param builder The session builder.
     * @param cart    List of cart items.
     */
    private void addCartItemsToBuilder(SessionCreateParams.Builder builder, List<CartItem> cart) {
        cart.forEach(item ->
                             builder.addLineItem(
                                     SessionCreateParams.LineItem.builder()
                                             .setQuantity((long) item.getQuantity())
                                             .setPrice(item.getProduct().getPriceId())
                                             .setAdjustableQuantity(
                                                     SessionCreateParams.LineItem.AdjustableQuantity.builder()
                                                             .setEnabled(true)
                                                             .setMinimum(1L)
                                                             .setMaximum(5L)
                                                             .build()
                                             )
                                             .build()
                             ));
    }

    /**
     * Creates a payment intent.
     *
     * @param amountTotal  The total amount to be charged.
     * @param currencyCode The currency code.
     * @return A Stripe PaymentIntent object.
     * @throws StripeException If an error occurs while creating the payment intent.
     */
    public PaymentIntent createPaymentIntent(PaymentIntentCreateParams.Shipping shipping,
                                             long amountTotal, String currencyCode,
                                             String courierId, String customerEmail,
                                             String addressIdx, String calculationId,
                                             String customer)
            throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCustomer(customer)
                .setAmount(amountTotal)
                .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.ON_SESSION)
                .setCurrency(currencyCode)
                //todo: remove after implementing creating payment methods in the settings
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(
                                true).build()
                )
                .putMetadata("courier_id", courierId)
                .putMetadata("email", customerEmail)
                .putMetadata("addressIdx", addressIdx)
                .putMetadata("calculation_id", calculationId)
                .setShipping(shipping)
                .build();

        return PaymentIntent.create(params);
    }

    public PaymentIntent createPaymentIntent(PaymentIntentCreateParams params)
            throws StripeException {

        return PaymentIntent.create(params);
    }

    public Refund createRefund(String paymentIntent, Long amount, RefundCreateParams.Reason reason)
            throws StripeException {


        RefundCreateParams params = RefundCreateParams.builder()
                .setAmount(amount)
                .setPaymentIntent(paymentIntent)
                .setReason(reason)
                .build();

        return Refund.create(params);
    }

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


    public Calculation calculate(CalculationCreateParams params)
            throws StripeException {
        return Calculation.create(params);
    }


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


    // my integration require payment intent - represent transaction on my server (assigned order
    // id) - status


    // include returning functionality in order ( create order details in which we can manage it)
//    Reason for Return: (when to return for shipping both sides) (possibility to generate label
//    in 2 ways - paid by me and by the client)
//
//    Defective or Wrong Product: If the product is defective or the wrong item was shipped,
//    refunding the shipping cost is generally considered good customer service.
//    Customer Changed Mind: If the return is due to the customer changing their mind or ordering
//    the wrong size/color, you may decide not to refund the shipping costs.

    // create tax transaction on new and refund
    // tax reports - available

    // refunds:
    // transactions management

    // customer one to many payment intent
    // creating of shopping cart - retrieve payment intent that's incomplete (only one incomplete
    // per customer) (once created payment intent remain 90 days)
    // updated date - created date required
    // refunded date
    // decline reason
    // customer email - in payment intent customer
    // amount
    // payment intent status
    // on success - order id

    // Refund
    // on success payment intent create tax transaction
    // refund - means reverse tax transaction Reversal transactions offset an earlier transaction
    // by having amounts with opposite signs
    // -- Append a suffix to the original reference. For example, if the original transaction has
    // reference pi_123456789, then create the reversal transaction with reference
    // pi_123456789-refund.
    // - Use the ID of the Stripe refund or a refund ID from your system. For example,
    // re_3MoslRBUZ691iUZ41bsYVkOg or myRefund_456.

    // full refund : When you fully refund a sale in your system, create a reversal transaction
    // with mode=full.
    // Partially refund a sale : After issuing a refund to your customer, create a reversal tax
    // transaction with mode=partial. This allows you to record a partial refund by providing the
    // line item amounts refunded. You can create up to 30 partial reversals for each sale.
    // Reversing more than the amount of tax you collected returns an error.

    // todo: save payment methods on server + adding in the client ui the payment methods for
    //  future use of the client
    // todo: handle delayed payments webhook - make this kind of payment available
    // todo: send receipt to customer
    // todo : refund : return whole - or the partial
    // todo: include info on product form that price must be included with stripe fee
    // method to change currency conversion based on location

    // FINISH TAX:
    // after the order is submitted create tax transaction
    // Listen for the payment_intent.succeeded webhook event. Retrieve the calculation ID from
    // the PaymentIntent metadata.
    // must store payment intent
    // store transaction it so later we can record refunds - store it in metadata paymentintent

    // Refunds
    // https://docs.stripe.com/tax/custom#reversals - refund tax transaction - or partially refund!
}
