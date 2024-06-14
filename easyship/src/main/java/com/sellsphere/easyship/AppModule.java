package com.sellsphere.easyship;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sellsphere.easyship.provider.ClientProvider;
import jakarta.ws.rs.client.Client;

import java.time.LocalDateTime;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        configureClient();
        bind(EasyshipService.class).to(EasyshipIntegrationService.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    private void configureClient() {
        bind(Client.class).toProvider(ClientProvider.class).in(Singleton.class);
    }
}