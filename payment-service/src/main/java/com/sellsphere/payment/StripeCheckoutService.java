package com.sellsphere.payment;

import com.sellsphere.common.entity.CartItem;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.List;

import static com.sellsphere.payment.Constants.DOMAIN;

public class StripeCheckoutService {

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
