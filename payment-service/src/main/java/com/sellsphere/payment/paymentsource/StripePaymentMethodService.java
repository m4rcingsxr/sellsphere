package com.sellsphere.payment.paymentsource;

import com.sellsphere.payment.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling Stripe customer operations.
 */
@Slf4j
public class StripePaymentMethodService {

    private final StripeConfig stripeConfig;

    @Inject
    public StripePaymentMethodService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    public void detachPaymentMethod(String stripePaymentMethodId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(stripePaymentMethodId);

        paymentMethod.detach();
    }

    public PaymentMethod attachPaymentMethod(String customerStripeId, String stripePaymentMethodId)
            throws StripeException {
        PaymentMethod resource = PaymentMethod.retrieve(stripePaymentMethodId);
        PaymentMethodAttachParams params =
                PaymentMethodAttachParams.builder().setCustomer(customerStripeId).build();
        return resource.attach(params);
    }

    public SetupIntent createSetupIntent(String stripeId) throws StripeException {
        SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                .setCustomer(stripeId)
                .addPaymentMethodType("card")
                .build();
        return SetupIntent.create(params);
    }
}
