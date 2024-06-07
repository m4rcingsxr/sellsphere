package com.sellsphere.payment;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.payment.payload.CalculationRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.Address;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Calculation;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.tax.CalculationCreateParams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
    public Session createCheckoutSession(List<CartItem> cart, List<Country> supportedCountries) throws StripeException {
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
                                .setTermsOfService(SessionCreateParams.ConsentCollection.TermsOfService.REQUIRED)
                                .build()
                );
    }

    /**
     * Adds the allowed countries to the session builder.
     *
     * @param builder            The session builder.
     * @param supportedCountries List of supported countries.
     */
    private void addAllowedCountriesToBuilder(SessionCreateParams.Builder builder, List<Country> supportedCountries) {
        List<SessionCreateParams.ShippingAddressCollection.AllowedCountry> allowedCountries = new ArrayList<>();
        for (Country supportedCountry : supportedCountries) {
            allowedCountries.add(
                    SessionCreateParams.ShippingAddressCollection.AllowedCountry.valueOf(supportedCountry.getCode())
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
    public PaymentIntent createPaymentIntent(long amountTotal, String currencyCode) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountTotal)
                .setCurrency(currencyCode)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();

        return PaymentIntent.create(params);
    }

    /**
     * Calculates the total price for the checkout process.
     *
     * @param request           The calculation request containing address, shipping cost, etc.
     * @param cart              List of cart items.
     * @param baseCurrencyCode  Base currency code.
     * @param targetCurrency    Target currency.
     * @return Calculation - The calculation result.
     * @throws StripeException If an error occurs during calculation.
     */
    public Calculation calculate(CalculationRequest request, List<CartItem> cart, Currency baseCurrencyCode, Currency targetCurrency) throws StripeException {
        var params = CalculationCreateParams.builder();
        Address address = request.getAddress();
        BigDecimal providedExchangeRate = request.getExchangeRate();
        BigDecimal shippingCost = request.getShippingCost();
        BigDecimal exchangeRate = providedExchangeRate != null ?
                providedExchangeRate.multiply(BigDecimal.ONE.add(Constants.CONVERT_CURRENCY_FEE)) : null;

        addCartItemsToCalculationParams(cart, params, baseCurrencyCode.getCode(), targetCurrency, exchangeRate);
        addShippingCostToCalculationParams(params, shippingCost, baseCurrencyCode.getCode(), targetCurrency, exchangeRate);
        addCustomerDetailsToCalculationParams(params, address);

        return Calculation.create(params.build());
    }

    /**
     * Adds the cart items to the calculation parameters.
     *
     * @param cart             List of cart items.
     * @param params           Calculation parameters builder.
     * @param baseCurrencyCode Base currency code.
     * @param targetCurrency   Target currency.
     * @param exchangeRate     Exchange rate.
     */
    private void addCartItemsToCalculationParams(List<CartItem> cart, CalculationCreateParams.Builder params,
                                                 String baseCurrencyCode, Currency targetCurrency, BigDecimal exchangeRate) {
        for (CartItem cartItem : cart) {
            long amount = baseCurrencyCode.equals(targetCurrency.getCode()) ?
                    roundAmount(cartItem.getProduct().getDiscountPrice(), targetCurrency.getUnitAmount()) :
                    roundAmount(cartItem.getProduct().getDiscountPrice().multiply(exchangeRate), targetCurrency.getUnitAmount());

            params.setCurrency(targetCurrency.getCode())
                    .addLineItem(
                            CalculationCreateParams.LineItem.builder()
                                    .setAmount(amount)
                                    .setTaxBehavior(CalculationCreateParams.LineItem.TaxBehavior.INCLUSIVE)
                                    .setReference(String.valueOf(cartItem.getProduct().getId()))
                                    .setTaxCode(cartItem.getProduct().getTax().getId())
                                    .build()
                    ).build();
        }
    }

    /**
     * Adds the shipping cost to the calculation parameters.
     *
     * @param params           Calculation parameters builder.
     * @param shippingCost     Shipping cost.
     * @param baseCurrencyCode Base currency code.
     * @param targetCurrency   Target currency.
     * @param exchangeRate     Exchange rate.
     */
    private void addShippingCostToCalculationParams(CalculationCreateParams.Builder params, BigDecimal shippingCost,
                                                    String baseCurrencyCode, Currency targetCurrency, BigDecimal exchangeRate) {
        long shippingAmount = baseCurrencyCode.equals(targetCurrency.getCode()) ?
                roundAmount(shippingCost, targetCurrency.getUnitAmount()) :
                roundAmount(shippingCost.multiply(exchangeRate), targetCurrency.getUnitAmount());

        params.setShippingCost(
                CalculationCreateParams.ShippingCost.builder()
                        .setTaxCode("txcd_92010001") // shipping tax code
                        .setTaxBehavior(CalculationCreateParams.ShippingCost.TaxBehavior.INCLUSIVE)
                        .setAmount(shippingAmount)
                        .build()
        );
    }

    /**
     * Adds the customer details to the calculation parameters.
     *
     * @param params  Calculation parameters builder.
     * @param address Shipping address.
     */
    private void addCustomerDetailsToCalculationParams(CalculationCreateParams.Builder params, Address address) {
        params.setCustomerDetails(
                CalculationCreateParams.CustomerDetails.builder()
                        .setAddress(
                                CalculationCreateParams.CustomerDetails.Address.builder()
                                        .setCountry(address.getCountry())
                                        .setCity(address.getCity())
                                        .setLine1(address.getLine1())
                                        .setLine2(address.getLine2())
                                        .setPostalCode(address.getPostalCode())
                                        .build()
                        )
                        .setAddressSource(CalculationCreateParams.CustomerDetails.AddressSource.SHIPPING)
                        .build()
        );
    }

    /**
     * Rounds a BigDecimal amount according to the currency unit amount.
     * For three-decimal currencies, it rounds to the nearest ten with the least-significant
     * digit as 0.
     * For two-decimal currencies, it rounds to two decimal places.
     * For zero-decimal currencies, it rounds to the nearest integer.
     *
     * @param amount     The amount to be rounded.
     * @param unitAmount The unit amount of the currency (1, 100, 1000).
     * @return The rounded and multiplied by unit amount long value.
     */
    public long roundAmount(BigDecimal amount, long unitAmount) {
        if (unitAmount == 1000) {
            // For three-decimal currencies, round to the nearest ten
            amount = amount.setScale(3, RoundingMode.HALF_UP);
            BigDecimal scaledAmount = amount.multiply(BigDecimal.valueOf(1000));
            BigDecimal roundedAmount = scaledAmount.divide(BigDecimal.TEN, 0,
                                                           RoundingMode.HALF_UP
            ).multiply(BigDecimal.TEN);
            return roundedAmount.longValue();
        } else if (unitAmount == 100) {
            // For two-decimal currencies, round to two decimal places
            return amount.multiply(BigDecimal.valueOf(unitAmount)).setScale(2, RoundingMode.HALF_UP).longValue();
        } else {
            // For zero-decimal currencies, round to the nearest integer
            return amount.multiply(BigDecimal.valueOf(unitAmount)).setScale(0, RoundingMode.HALF_UP).longValue() * unitAmount;
        }
    }

    /**
     * Calculates the total amount for the cart.
     *
     * @param cart     List of cart items.
     * @param currency Currency object.
     * @return The total amount.
     */
    public long getAmountTotal(List<CartItem> cart, Currency currency) {
        long total = 0;
        for (CartItem cartItem : cart) {
            total += CalculationCreateParams.LineItem.builder()
                    .setAmount(
                            cartItem.getProduct()
                                    .getDiscountPrice()
                                    .multiply(BigDecimal.valueOf(currency.getUnitAmount()))
                                    .longValue()
                    )
                    .setTaxCode(cartItem.getProduct().getTax().getId())
                    .build().getAmount();
        }

        return total;
    }

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
