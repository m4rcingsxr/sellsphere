package com.sellsphere.client.webhook;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Arrays;

@Getter
@Configuration
public class WebhookConfig {

    // cli locally, dashboard public
    private final String endpointSecret = System.getenv("ENDPOINT_SECRET");
    private final List<String> allowedIps;

    public WebhookConfig(@Value("${webhook.allowed-ips}") String allowedIps) {
        this.allowedIps = Arrays.asList(allowedIps.split(","));
    }

}
