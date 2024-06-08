package com.sellsphere.easyship;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sellsphere.easyship.provider.ClientProvider;
import com.sellsphere.easyship.provider.GsonProvider;
import jakarta.ws.rs.client.Client;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        configureGson();
        configureClient();
        bind(EasyshipService.class).to(EasyshipIntegrationService.class).in(Singleton.class);
    }

    public void configureGson() {
        bind(Gson.class).toProvider(GsonProvider.class).in(Singleton.class);
    }

    private void configureClient() {
        bind(Client.class).toProvider(ClientProvider.class).in(Singleton.class);
    }

}
