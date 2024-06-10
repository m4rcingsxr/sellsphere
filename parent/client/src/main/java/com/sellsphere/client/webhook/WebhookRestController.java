package com.sellsphere.client.webhook;

import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
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

    // Webhook endpoints might occasionally receive the same event more than once
    private final ConcurrentHashMap<String, Boolean> processedEvents = new ConcurrentHashMap<>();

    // test env - not public endpoint
    @PostMapping("/webhook")
    public ResponseEntity<String> handleCheckoutWebhook(@RequestBody String payload,
                                                        HttpServletRequest request)
            throws IOException {
        String requestIp = request.getRemoteAddr();

        if (!isAllowedIp(requestIp)) {
            log.warn("Unauthorized IP address: {}", requestIp);
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

        if(webhookConfig.getEndpointSecret() != null && sigHeader != null) {
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
            taskExecutor.submit(() -> processEvent(stripeObject, finalEvent));
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            throw new IOException("Deserialization failed, probably due to an API version mismatch");
        }


        return ResponseEntity.ok().build();
    }

    private void processEvent(Optional<StripeObject> stripeObjectOpt, Event event) {
        StripeObject stripeObject;
        if (stripeObjectOpt.isPresent()) {
            stripeObject = stripeObjectOpt.get();
        } else {
            log.error("Deserialization of event data failed.");
            return;
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                handlePaymentIntentSucceeded(paymentIntent);
                break;
            case "payment_method.attached":
                PaymentMethod paymentMethod = (PaymentMethod) stripeObject;
                handlePaymentMethodAttached(paymentMethod);
                break;
            // ... handle other event types
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private boolean isAllowedIp(String ip) {
        return webhookConfig.getAllowedIps().contains(ip);
    }


    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        // create order
        // send recipient email
        log.info("Handled payment intent succeeded for: {}", paymentIntent.getId());
    }

    private void handlePaymentMethodAttached(PaymentMethod paymentMethod) {
        // Implement your logic here
        log.info("Handled payment method attached for: {}", paymentMethod.getId());
    }

}
