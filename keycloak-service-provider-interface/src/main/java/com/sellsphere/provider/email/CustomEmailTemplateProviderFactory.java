package com.sellsphere.provider.email;

import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.EmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class CustomEmailTemplateProviderFactory implements EmailTemplateProviderFactory {

    @Override
    public EmailTemplateProvider create(KeycloakSession session) {
        return new CustomEmailTemplateProvider(session);
    }

    @Override
    public String getId() {
        return "custom-email-template";
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }
}