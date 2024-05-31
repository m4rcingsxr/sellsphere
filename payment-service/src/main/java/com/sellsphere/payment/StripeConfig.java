package com.sellsphere.payment;

import com.stripe.Stripe;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StripeConfig {

    public static void init() {
        if(Stripe.apiKey == null) {
            Stripe.apiKey = System.getenv("STRIPE_API_KEY");
        }
    }

}
