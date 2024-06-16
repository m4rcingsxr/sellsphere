package com.sellsphere.provider.customer;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.CDI;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class CustomerProviderFactory implements UserStorageProviderFactory<CustomerProvider> {

    private static final String PROVIDER_ID = "customer-storage-provider";

    @Override
    public CustomerProvider create(KeycloakSession session, ComponentModel model) {
        JpaConnectionProvider jpaConnectionProvider = session.getProvider(JpaConnectionProvider.class, "user-store");
        Event<UserAddedEvent> userAddedEvent = CDI.current().select(Event.class).get();
        return new CustomerProvider(session, model, jpaConnectionProvider, userAddedEvent);
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