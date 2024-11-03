package com.sellsphere.client.security;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sellsphere.payment.StripeModule;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/wishlist/**").authenticated()
                        .requestMatchers("/questions/**").authenticated()
                        .requestMatchers("/checkout/**").authenticated()
                        .requestMatchers("/addresses/**").authenticated()
                        .requestMatchers("/addresses/**").authenticated()
                        .requestMatchers("/reviews").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/webhook") // Disable CSRF for webhook endpoint
                )
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

    @Bean
    public StripeCheckoutService stripeService() {
        Injector injector = Guice.createInjector(new StripeModule());
        return injector.getInstance(StripeCheckoutService.class);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(100);
    }

}



