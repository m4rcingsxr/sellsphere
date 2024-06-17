package com.sellsphere.client.webhook;

import com.google.gson.JsonSyntaxException;
import com.sellsphere.client.customer.CustomerRepository;
import com.sellsphere.client.order.OrderService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.tax.Transaction;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookRestController {

    private final WebhookConfig webhookConfig;
    private final ExecutorService taskExecutor;
    private final OrderService orderService;
    private final StripeCheckoutService stripeService;

    // Webhook endpoints might occasionally receive the same event more than once
    private final ConcurrentHashMap<String, Boolean> processedEvents = new ConcurrentHashMap<>();
    private final CustomerRepository customerRepository;
    private final SettingService settingService;

    // test env - not public endpoint
    @PostMapping("/webhook")
    public ResponseEntity<String> handleCheckoutWebhook(@RequestBody String payload,
                                                        HttpServletRequest request)
            throws IOException, CustomerNotFoundException, CurrencyNotFoundException {
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

            String currency = settingService.getCurrencyCode();

            final Event finalEvent = event;
            taskExecutor.submit(() -> {
                try {
                    processEvent(stripeObject, finalEvent, currency);
                } catch (Exception e) {
                    log.error("Customer not found", e);
                    throw new RuntimeException(e);
                }
            });
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            throw new IOException(
                    "Deserialization failed, probably due to an API version mismatch");
        }


        return ResponseEntity.ok().build();
    }

    private void processEvent(Optional<StripeObject> stripeObjectOpt, Event event, String currency)
            throws CustomerNotFoundException, AddressNotFoundException, CountryNotFoundException,
            StripeException {

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
                handlePaymentIntentSucceeded(paymentIntent,  currency);
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


    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent,
                                              String currency)
            throws CustomerNotFoundException, AddressNotFoundException, CountryNotFoundException,
            StripeException {
        // create order

        // metadata - courier_id (for selected rate)
        // deliverDays
        // deliverDate (naive)

        // customer
        // order details - from customer cart - delete cart on webhook (not in ui)

//        Order.builder()
//                .orderTime(LocalDateTime.now())
//                .
//                .build();

        // retrieve courier id from paymentIntent metadata

        ShippingDetails shipping = paymentIntent.getShipping();
        String courierId = paymentIntent.getMetadata().get("courier_id");
        String customerEmail = paymentIntent.getMetadata().get("email");
        String addressIdx = paymentIntent.getMetadata().get("addressIdx");

        orderService.createOrder(customerEmail,
                                 Address.builder()
                                         .line1(shipping.getAddress().getLine1())
                                         .line2(shipping.getAddress().getLine2())
                                         .city(shipping.getAddress().getCity())
                                         .state(shipping.getAddress().getState())
                                         .countryAlpha2(shipping.getAddress().getCountry())
                                         .contactName(shipping.getName())
                                         .postalCode(shipping.getAddress().getPostalCode())
                                         // send always to customer, not recipient
                                         .contactEmail(customerEmail)
                                         .contactPhone(shipping.getPhone())
                                         .build()
                , courierId, currency, addressIdx
        );

        Transaction transaction = stripeService.createTransaction(paymentIntent);

        PaymentIntentUpdateParams params =
                PaymentIntentUpdateParams.builder()
                        .putMetadata("tax_transaction", transaction.getId())
                        .build();

        paymentIntent.update(params);

        // send recipient email
        log.info("Handled payment intent succeeded for: {}", paymentIntent.getId());
    }

    private void handlePaymentMethodAttached(PaymentMethod paymentMethod) {

        log.info("Handled payment method attached for: {}", paymentMethod.getId());
    }



}
