package com.sellsphere.client.product;

import org.springframework.beans.factory.annotation.Autowired;
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

}