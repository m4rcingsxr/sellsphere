package com.sellsphere.admin.security;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sellsphere.easyship.AppModule;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.payment.StripeModule;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class WebSecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String EDITOR = "EDITOR";
    private static final String SALESPERSON = "SALESPERSON";
    private static final String SHIPPER = "SHIPPER";
    private static final String ASSISTANT = "ASSISTANT";
    private static final String REMEMBER_ME_KEY = "aP3rSi5t3nTS3cur3K3y";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configureAuthorizations(http);
        configureCsrf(http);
        configureCors(http);
        configureFormLogin(http);
        configureLogout(http);
        configureRememberMe(http);

        return http.build();
    }


    private void configureAuthorizations(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/node_modules/**",
                                 "/js/**", "/images/**"
                ).permitAll()
                .requestMatchers("/login-as").permitAll()
                .requestMatchers("/users/**").hasRole(ADMIN)
                .requestMatchers("/categories/**").hasAnyRole(ADMIN, EDITOR)
                .requestMatchers("/brands/**").hasAnyRole(ADMIN, EDITOR)
                .requestMatchers("/products/**").hasAnyRole(ADMIN, EDITOR)
                .requestMatchers("/products/page/**", "/products/details/**").hasAnyRole(SHIPPER, SALESPERSON)
                .requestMatchers("/settings/**").hasRole(ADMIN)
                .requestMatchers("/customers/**").hasAnyRole(ADMIN, SALESPERSON)
                .requestMatchers("/transactions/**").hasAnyRole(ADMIN, SALESPERSON)
                .requestMatchers("/customers/**").hasAnyRole(ADMIN, SALESPERSON)
                .requestMatchers("/orders/**").hasAnyRole(ADMIN,SALESPERSON, SHIPPER)
                .requestMatchers("/questions/**").hasAnyRole(ADMIN, ASSISTANT)
                .requestMatchers("/reviews/**").hasAnyRole(ADMIN, ASSISTANT)
                .requestMatchers("/articles/**").hasAnyRole(ADMIN, EDITOR)
                .requestMatchers("/promotions/**").hasAnyRole(ADMIN, EDITOR)
                .requestMatchers("/main_page/**").hasAnyRole(ADMIN, EDITOR)
                .requestMatchers("/reports/**").hasAnyRole(ADMIN, SALESPERSON)
                .anyRequest().authenticated());
    }

    private void configureCors(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
    }

    private void configureCsrf(HttpSecurity http) throws Exception {
        http.csrf(Customizer.withDefaults());
    }

    private void configureFormLogin(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticateTheUser")
                .defaultSuccessUrl("/")
                .permitAll()
        );
    }

    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .permitAll()
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );
    }

    private void configureRememberMe(HttpSecurity http) throws Exception {
        http.rememberMe(rememberMe -> rememberMe
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(365 * 24 * 60 * 60)
                .key(REMEMBER_ME_KEY)
        );
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(
                dataSource);

        userDetailsManager.setUsersByUsernameQuery(
                "select email, user_password, enabled from users where email = ?"
        );

        userDetailsManager.setAuthoritiesByUsernameQuery(
                "SELECT u.email, r.name " +
                        "FROM users u " +
                        "JOIN users_roles ur ON u.id = ur.user_id " +
                        "JOIN roles r ON ur.role_id = r.id " +
                        "WHERE u.email = ?"
        );


        return userDetailsManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public StripeCheckoutService stripeCheckoutService() {
        Injector injector = Guice.createInjector(new StripeModule());
        return injector.getInstance(StripeCheckoutService.class);
    }

    @Bean
    public EasyshipService easyshipService() {
        Injector injector = Guice.createInjector(new AppModule());
        return injector.getInstance(EasyshipService.class);
    }

}
