package com.sellsphere.provider.customer;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class CustomerProviderFactory implements UserStorageProviderFactory<CustomerProvider> {

    private static final String PROVIDER_ID = "customer-storage-provider";

    @Override
    public CustomerProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomerProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "JPA Example Customer Storage Provider";
    }
}
