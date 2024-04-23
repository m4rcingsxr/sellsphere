package com.sellsphere.client.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

/**
 * Implementation of LogoutSuccessHandler to handle successful logout requests.
 */
@AllArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {


    private String keycloakServerUrl;
    private String realmName;
    private String logoutRedirectUri;

    /**
     * Redirects the user to the Keycloak logout URL after successful logout.
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws
            IOException {
        String logoutUrl = keycloakServerUrl + "/realms/" + realmName + "/protocol/openid-connect/logout?redirect_uri=" + logoutRedirectUri;
        response.sendRedirect(logoutUrl);
    }
}