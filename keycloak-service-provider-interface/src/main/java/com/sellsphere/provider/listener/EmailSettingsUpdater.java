package com.sellsphere.provider.listener;

import lombok.experimental.UtilityClass;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class EmailSettingsUpdater {

    private static final String REALM_NAME = "SellSphere";

    public static void updateRealmSMTPConfig(KeycloakSession session,
                                             EmailSettingManager emailSettings) {

        RealmModel realm = session.realms().getRealmByName(REALM_NAME);


        Map<String, String> smtpConfig = new HashMap<>();
        smtpConfig.put("from", emailSettings.getMailFrom()); // The actual email address used in 'from'

        smtpConfig.put("host", emailSettings.getHost());
        smtpConfig.put("port", String.valueOf(emailSettings.getPort()));
        smtpConfig.put("user", emailSettings.getUsername());
        smtpConfig.put("password", emailSettings.getPassword());
        smtpConfig.put("auth", String.valueOf(emailSettings.getEnableSmtpAuthentication()));
        smtpConfig.put("starttls", String.valueOf(emailSettings.getEnableTLS()));

        realm.setSmtpConfig(smtpConfig);
    }
}
