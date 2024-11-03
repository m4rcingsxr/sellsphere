package com.sellsphere.easyship;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EasyshipService.class).to(EasyshipIntegrationService.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public Client provideClient() {
        return ClientBuilder.newClient();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
}
