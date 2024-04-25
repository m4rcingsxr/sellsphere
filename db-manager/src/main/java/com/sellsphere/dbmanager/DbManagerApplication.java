package com.sellsphere.dbmanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

@SpringBootApplication
public class DbManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbManagerApplication.class, args);
    }

    // exit after initializations
    @Bean
    public CommandLineRunner run(
            ApplicationContext context) {
        return args -> SpringApplication.exit(context, () -> 0);
    }
}
