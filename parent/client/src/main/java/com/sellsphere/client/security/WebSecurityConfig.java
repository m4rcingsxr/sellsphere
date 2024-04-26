package com.sellsphere.client.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${app.logout.redirect.uri}")
    private String logoutRedirectUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/customer/**").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(Customizer.withDefaults())
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login")
                        .defaultSuccessUrl("/customer?continue", true))
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout
                        .permitAll()
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler(
                                new LogoutSuccessHandlerImpl(
                                        keycloakServerUrl,
                                        realmName,
                                        logoutRedirectUri
                                )));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}



