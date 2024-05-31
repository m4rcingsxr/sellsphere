package com.sellsphere.payment;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Country;
import com.stripe.exception.StripeException;
import com.stripe.model.Address;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Calculation;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.tax.CalculationCreateParams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class StripeCheckoutService {

    static {
        StripeConfig.init();
    }

    private static final String DOMAIN = "http://localhost:8081/SellSphere";

    public Session createCheckoutSession(List<CartItem> cart,
                                         List<Country> supportedCountries) throws StripeException {

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
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
                );


        List<SessionCreateParams.ShippingAddressCollection.AllowedCountry> allowedCountries =
                new ArrayList<>();
        for (Country supportedCountry : supportedCountries) {

            allowedCountries.add(
                    SessionCreateParams.ShippingAddressCollection.AllowedCountry.valueOf(
                            supportedCountry.getCode()));
        }


        builder.setShippingAddressCollection(SessionCreateParams.ShippingAddressCollection.builder()
                                                     .addAllAllowedCountry(allowedCountries)
                                                     .build());
//        CouponCreateParams couponParams =
//                CouponCreateParams.builder()
//                        .setPercentOff(new BigDecimal(100))
//                        .setDuration(CouponCreateParams.Duration.ONCE)
//                        .build();
//
//        Coupon coupon = Coupon.create(couponParams);


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
        builder
//                .addDiscount(
//                        SessionCreateParams.Discount.builder().setCoupon(coupon.getId()).build()
//                )
                .setConsentCollection(
                        SessionCreateParams.ConsentCollection.builder()
                                .setTermsOfService(
                                        SessionCreateParams.ConsentCollection.TermsOfService.REQUIRED)
                                .build()
                );

        SessionCreateParams params = builder.build();
        return Session.create(params);
    }

    // defer initialization to final user pay -  provide final shipping when user click pay
    public PaymentIntent createPaymentIntent() throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
//                        .setReceiptEmail(customer.getEmail()) - receipt -  Stripe sends a receipt to that address regardless of your Customer emails settings.
                        // customize receipt https://docs.stripe.com/receipts?payment-ui=direct-api
                        .setAmount
                                (1099L)
                        .setCurrency
                                ("eur")
                        .setAutomaticPaymentMethods
                                (
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled
                                                (true).build()
                                )
                        .build();

        return PaymentIntent.create(params);
    }

    // cart, address, shipping cost - per item - calculate in this method
    // calculate it every time address is changed
    // provide correct currency - user can dynamically change it
    // verify if address is correct
    public Calculation calculateTax(List<CartItem> cart, Address address) throws StripeException {
        var params = CalculationCreateParams.builder();

        for (CartItem cartItem : cart) {
            params.setCurrency
                            ("eur")
//                    .setShippingCost() // set shippingm cost for each item - require shipping rate which will be from easy post
                    .addLineItem
                            (
                                    CalculationCreateParams.LineItem.builder()
                                            .setAmount
                                                    (cartItem.getProduct().getDiscountPrice().multiply(
                                                            BigDecimal.valueOf(100)).longValue()) // static 100 for now
                                            .setTaxBehavior
                                                    (CalculationCreateParams.LineItem.TaxBehavior.INCLUSIVE)
                                            .setReference(String.valueOf(cartItem.getProduct().getId()))
                                            .setTaxCode
                                                    (cartItem.getProduct().getTax().getId())
                                            .build()
                            )
                    .setCustomerDetails
                            (
                                    CalculationCreateParams.CustomerDetails.builder()
                                            .setAddress
                                                    (
                                                            CalculationCreateParams.CustomerDetails.Address.builder()
                                                                    .setCountry
                                                                            (address.getCountry())
                                                                    .setCity(address.getCity())
                                                                    .setLine1(address.getLine1())
                                                                    .setLine1(address.getLine2())
                                                                    .setPostalCode(address.getPostalCode())

                                                                    .build()
                                                    )
                                            .setAddressSource
                                                    (CalculationCreateParams.CustomerDetails.AddressSource.SHIPPING)
                                            .build()
                            ).build();
        }

        return Calculation.create(params.build());
    }

    // method to change currency based on location

    // FINISH TAX:
    // after the order is submitted create tax transaction
    // Listen for the payment_intent.succeeded webhook event. Retrieve the calculation ID from the PaymentIntent metadata.
    // must store payment intent
    // store transaction it so later we can record refunds - store it in metadata paymentintent

    // Refunds
    // https://docs.stripe.com/tax/custom#reversals - refund tax transaction - or partially refund!



}
