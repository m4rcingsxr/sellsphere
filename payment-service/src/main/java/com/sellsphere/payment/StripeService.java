package com.sellsphere.payment;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Currency;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;
import java.util.List;

public class StripeService {

    public static final String DOMAIN = "http://localhost:8081/SellSphere";


    static {
        Stripe.apiKey =
                "sk_test_51PailoF45HI7vp7sJPtRQJn32yhMaPbqUITsFkNtbdKogwcUTeVvTcNXjMPHigCTXSqaSLfuWNuFR1KDGISGNlKa00a9EokJ6w";
    }

    public Product createProduct(com.sellsphere.common.entity.Product product, Currency currency)
            throws StripeException {
        ProductCreateParams params =
                ProductCreateParams.builder()
                        .setId(String.valueOf(product.getId()))
                        .setName(product.getName())
                        .setDefaultPriceData(
                                ProductCreateParams.DefaultPriceData.builder()
                                        .setUnitAmount(product.getDiscountPrice().multiply(
                                                BigDecimal.valueOf(
                                                        currency.getUnitAmount())).longValue())
                                        .setCurrency(currency.getCode())
//                                        .setTaxBehavior() set tax behavior based on retrieved setting
                                        .build()
                        )
                        .addExpand("default_price")
                        .build();

        return Product.create(params);
    }

    public Session createCheckoutSession(List<CartItem> cart) throws StripeException {

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
//                .setCustomer
//                        ("{{CUSTOMER_ID}}"
//                        )
//                .setCustomerUpdate
//                        (
//                                SessionCreateParams.CustomerUpdate.builder()
//                                        .setAddress
//                                                (SessionCreateParams.CustomerUpdate.Address.AUTO)
//                                        .build()
//                        )
                .setReturnUrl(DOMAIN + "/checkout/return?session_id={CHECKOUT_SESSION_ID}")
                .setAutomaticTax(
                        SessionCreateParams.AutomaticTax.builder()
                                .setEnabled(true)
                                .build()
                );

        cart.forEach(item ->
                             builder.addLineItem(
                                     SessionCreateParams.LineItem.builder()
                                             .setQuantity((long) item.getQuantity())
                                             .setPrice(item.getProduct().getPriceId())
                                             .build()
                             ));

        SessionCreateParams params = builder.build();
        return Session.create(params);
    }
}
