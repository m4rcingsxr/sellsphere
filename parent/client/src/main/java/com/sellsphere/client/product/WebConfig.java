package com.sellsphere.client.product;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sellsphere.easyship.ApiService;
import com.sellsphere.easyship.AppModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ProductFilterArgumentResolver productFilterArgumentResolver;

    @Autowired
    public WebConfig(@Lazy ProductFilterArgumentResolver productFilterArgumentResolver) {
        this.productFilterArgumentResolver = productFilterArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(productFilterArgumentResolver);
    }

    @Bean
    public ApiService easyshipService() {
        Injector injector = Guice.createInjector(new AppModule());
        return injector.getInstance(ApiService.class);
    }

}