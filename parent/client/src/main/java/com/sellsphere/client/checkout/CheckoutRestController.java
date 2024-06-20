package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.CalculationRequestDTO;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.sellsphere.common.entity.payload.CalculationResponseDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.CustomerSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


/**
 * REST controller for handling checkout-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutRestController {

    private final CustomerService customerService;
    private final CheckoutService checkoutService;
    private final TransactionService transactionService;

    /**
     * Calculates the total cost including address-specific details with a specified exchange
     * rate or in base currency if the rate is not defined.
     *
     * @param request   The calculation request containing address details.
     * @param principal The authenticated user's principal.
     * @return The calculation response containing total amounts and details.
     * @throws StripeException           if there is an error with Stripe operations.
     * @throws CustomerNotFoundException if the customer is not found.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponseDTO> calculate(
            @RequestBody CalculationRequestDTO request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException,
            SettingNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        CalculationResponseDTO response = checkoutService.calculate(request, customer);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a payment intent for the given payment request.
     *
     * @param request The payment request containing address, amount and currency details.
     * @return A map containing the client secret of the payment intent.
     * @throws StripeException if there is an error with Stripe operations.
     */
    @PostMapping("/save-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestBody PaymentRequestDTO request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException,
            TransactionNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        String clientSecret = transactionService.savePaymentIntent(request, customer);

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", clientSecret);

        return ResponseEntity.ok(map);
    }

    // fetch payment intent existing & modified for current state or create new (start checkout process)
    @PostMapping("/init-payment-intent")
    public ResponseEntity<Map<String, String>> fetchPaymentIntent(Principal principal)
            throws CustomerNotFoundException, StripeException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        String clientSecret = transactionService.initializePaymentIntent(customer);

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", clientSecret);

        return ResponseEntity.ok(map);
    }

    @PostMapping("/create-customer-session")
    public ResponseEntity<Map<String, String>> createCustomerSession(Principal principal)
            throws CustomerNotFoundException, StripeException {
        Customer customer = getAuthenticatedCustomer(principal);

        CustomerSession session = checkoutService.createCustomerSession(customer);

        Map<String, String> map = new HashMap<>();
        map.put("customerSessionClientSecret", session.getClientSecret());

        return ResponseEntity.ok(map);
    }


    /**
     * Retrieves the authenticated customer from the principal.
     *
     * @param principal The authenticated user's principal.
     * @return The customer entity.
     * @throws CustomerNotFoundException if the customer is not found.
     */
    private Customer getAuthenticatedCustomer(Principal principal) throws
            CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }
}