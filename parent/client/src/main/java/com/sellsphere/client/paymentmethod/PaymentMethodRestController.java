package com.sellsphere.client.paymentmethod;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.webhook.CardRepository;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("payment-methods")
@RequiredArgsConstructor
public class PaymentMethodRestController {

    private final CustomerService customerService;
    private final PaymentMethodService paymentMethodService;
    private final CardRepository cardRepository;


    @PostMapping("/attach")
    public ResponseEntity<Map<String, String>> attachPaymentMethod(@RequestBody Map<String, String> payload, Principal principal)
            throws CustomerNotFoundException, StripeException {
        paymentMethodService.attachPaymentMethod(getAuthenticatedCustomer(principal), payload.get("paymentMethod"));

        return ResponseEntity.ok(Map.ofEntries(Map.entry("status","success")));
    }

    @PostMapping("/detach")
    public ResponseEntity<Map<String, String>> detachPaymentMethod(@RequestBody Map<String, String> payload)
            throws  StripeException {
        paymentMethodService.detachPaymentMethod(payload.get("paymentMethodId"));
        return ResponseEntity.ok(Map.ofEntries(Map.entry("status","success")));
    }

    @PostMapping("/setup-intent")
    public ResponseEntity<Map<String, String>> getSetupIntent(Principal principal)
            throws CustomerNotFoundException, StripeException {
        Customer customer = getAuthenticatedCustomer(principal);
        SetupIntent setupIntent = paymentMethodService.getSetupIntent(customer);
        return ResponseEntity.ok(Map.of("clientSecret", setupIntent.getClientSecret()));
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
