package com.sellsphere.payment;

import com.stripe.Stripe;
import jakarta.inject.Singleton;

/**
 * Utility class for initializing Stripe configuration.
 */
@Singleton
public class StripeConfig {

    public StripeConfig() {
        if (Stripe.apiKey == null) {
            Stripe.apiKey = System.getenv("STRIPE_API_KEY");
        }
    }
}