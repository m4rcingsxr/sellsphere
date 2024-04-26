package com.sellsphere.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.sellsphere.client.customer",
    entityManagerFactoryRef = "keycloakEntityManagerFactory",
    transactionManagerRef = "keycloakTransactionManager"
)
public class KeycloakDatabaseConfig {

    @Bean(name = "keycloakDataSource")
    public DataSource keycloakDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/keycloak")
                .username("sellsphere")
                .password("o7gpbK[O(-__=1h@k+OhS(@cdBEZTir!xM0Gxwhr;dU0aT7DaV")
                .build();
    }

    @Bean(name = "keycloakEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean keycloakEntityManagerFactory(
            @Qualifier("keycloakDataSource") DataSource keycloakDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(keycloakDataSource);
        em.setPackagesToScan("com.sellsphere.common.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    @Bean(name = "keycloakTransactionManager")
    public PlatformTransactionManager keycloakTransactionManager(
            @Qualifier("keycloakEntityManagerFactory") LocalContainerEntityManagerFactoryBean keycloakEntityManagerFactory) {
        return new JpaTransactionManager(keycloakEntityManagerFactory.getObject());
    }
}