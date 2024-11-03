package com.sellsphere.provider.listener;

import com.sellsphere.provider.customer.external.Setting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import java.util.List;

public class EmailSettingsEventListenerProvider
        implements EventListenerProvider {


    private final KeycloakSession session;
    protected final EntityManager entityManager;

    public EmailSettingsEventListenerProvider(KeycloakSession session) {
        this.session = session;
        entityManager = session.getProvider(JpaConnectionProvider.class,
                                            "user-store"
        ).getEntityManager();
    }


    @Override
    public void onEvent(Event event) {
        if(event.getType().equals(EventType.REGISTER) || event.getType().equals(EventType.SEND_VERIFY_EMAIL)) {
            EmailSettingManager emailSettings = loadEmailSettings();
            EmailSettingsUpdater.updateRealmSMTPConfig(session, emailSettings);
        }
    }

    private EmailSettingManager loadEmailSettings() {
        String jpql = "SELECT s FROM Setting s WHERE s.category = 'MAIL_TEMPLATES' OR s.category = 'MAIL_SERVER'";
        TypedQuery<Setting> query = entityManager.createQuery(jpql, Setting.class);
        List<Setting> settings = query.getResultList();
        return new EmailSettingManager(settings);
    }

    // admin events
    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        System.out.println(event);
    }

    @Override
    public void close() {

    }
}
