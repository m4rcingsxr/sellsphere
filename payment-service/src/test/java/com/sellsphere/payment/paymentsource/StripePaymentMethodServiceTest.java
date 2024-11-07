package com.sellsphere.payment.paymentsource;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sellsphere.payment.StripeModule;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StripePaymentMethodServiceTest {

    @Inject
    private StripePaymentMethodService paymentService;

    @BeforeEach
    public void setup() {
        Injector injector = Guice.createInjector(new StripeModule());
        injector.injectMembers(this);
    }


}