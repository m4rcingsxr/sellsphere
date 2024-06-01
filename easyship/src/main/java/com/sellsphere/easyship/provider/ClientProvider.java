package com.sellsphere.easyship.provider;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

@Singleton
public class ClientProvider implements Provider<Client> {

    // prototype scope
    @Override
    public Client get() {
        return ClientBuilder.newClient();
    }

}
