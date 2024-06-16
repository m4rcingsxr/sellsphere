package com.sellsphere.provider.customer;

import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class CustomerProviderFactory implements UserStorageProviderFactory<CustomerProvider> {

    static final String USER_CREATION_ENABLED = "userCreation";
    private static final String PROVIDER_ID = "customer-storage-provider";

    @Override
    public CustomerProvider create(KeycloakSession session, ComponentModel model) {
        JpaConnectionProvider jpaConnectionProvider = session.getProvider(JpaConnectionProvider.class, "user-store");
        return new CustomerProvider(session, model, jpaConnectionProvider);
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