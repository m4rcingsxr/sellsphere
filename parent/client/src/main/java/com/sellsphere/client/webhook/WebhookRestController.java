
package com.sellsphere.client.webhook;

import com.google.gson.JsonSyntaxException;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookRestController {

    private final WebhookConfig webhookConfig;
    private final ExecutorService taskExecutor;
    private final WebhookService webhookService;

    // Webhook endpoints might occasionally receive the same event more than once
    private final ConcurrentHashMap<String, Boolean> processedEvents = new ConcurrentHashMap<>();

    /**
     * Handles incoming Stripe webhooks.
     *
     * @param payload The webhook payload.
     * @param request The HTTP request.
     * @return The response entity.
     * @throws IOException if an I/O error occurs.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleCheckoutWebhook(@RequestBody String payload,
                                                        HttpServletRequest request)
            throws IOException {
        String requestIp = request.getRemoteAddr();

        if (!isAllowedIp(requestIp)) {
            log.error("Unauthorized IP address: {}", requestIp);
            return ResponseEntity.status(403).body("Forbidden");
        }


        Event event;

        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            log.warn("⚠️  Webhook error while parsing basic request.");
            return ResponseEntity.badRequest().body("");
        }

        String sigHeader = request.getHeader("Stripe-Signature");

        if (webhookConfig.getEndpointSecret() != null && sigHeader != null) {

            // Only verify the event if you have an endpoint secret defined.
            // Otherwise use the basic event deserialized with GSON.
            try {
                event = Webhook.constructEvent(
                        payload, sigHeader, webhookConfig.getEndpointSecret()
                );
            } catch (SignatureVerificationException e) {
                // Invalid signature
                log.warn("⚠️  Webhook error while validating signature.");
                return ResponseEntity.badRequest().body("");
            }
        }

        // Check if the event has already been processed
        if (processedEvents.putIfAbsent(event.getId(), Boolean.TRUE) != null) {
            log.info("Duplicate event received: {}", event.getId());
            return ResponseEntity.ok("Duplicate event ignored");
        }


        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            Optional<StripeObject> stripeObject = dataObjectDeserializer.getObject();

            final Event finalEvent = event;
            taskExecutor.submit(wrapCheckedException(() -> webhookService.processEvent(stripeObject, finalEvent)));

        } else {

            // Deserialization failed, probably due to an API version mismatch.
            throw new IOException("Deserialization failed, probably due to an API version mismatch");
        }

        return ResponseEntity.ok().build();
    }



    private boolean isAllowedIp(String ip) {
        return webhookConfig.getAllowedIps().contains(ip);
    }


    public static <T> Runnable wrapCheckedException(CheckedExceptionRunnable<T> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Exception thrown during execution of webhook task", e);
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    public interface CheckedExceptionRunnable<T> {
        void run() throws Exception;
    }

}

