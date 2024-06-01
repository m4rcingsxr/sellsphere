package com.sellsphere.easyship.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class GsonProvider implements Provider<Gson> {

    private static final class GsonHolder {
        private static final Gson INSTANCE =  new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public Gson get() {
        return GsonHolder.INSTANCE;
    }

}
