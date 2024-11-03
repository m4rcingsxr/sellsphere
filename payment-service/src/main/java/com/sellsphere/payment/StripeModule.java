package com.sellsphere.payment;

import com.google.inject.AbstractModule;
import jakarta.inject.Singleton;

public class StripeModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind StripeConfig as a singleton
        bind(StripeConfig.class).in(Singleton.class);
    }

}
