package com.sellsphere.client.checkout;

import com.sellsphere.client.RecaptchaVerificationService;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.customer.RecaptchaVerificationFailed;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.CalculationRequestDTO;
import com.sellsphere.common.entity.payload.CalculationResponseDTO;
import com.sellsphere.common.entity.payload.CheckoutDTO;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.CustomerSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling checkout-related operations.
 * It provides endpoints for calculating totals, creating payment intents, and managing Stripe sessions.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutRestController {

    private final CustomerService customerService;
    private final CheckoutService checkoutService;
    private final TransactionService transactionService;

    /**
     * Calculates the total cost of the cart including taxes and shipping, based on the customer's address
     * and the selected currency. This method interacts with Stripe to retrieve real-time tax calculations.
     *
     * @param request   The calculation request DTO containing address and cart details.
     * @param principal The authenticated user's principal (to identify the customer).
     * @return A ResponseEntity containing the detailed calculation response (totals, taxes, etc.).
     * @throws StripeException           If an error occurs while interacting with Stripe.
     * @throws CustomerNotFoundException If the customer is not found in the system.
     * @throws CurrencyNotFoundException If the specified currency is invalid or not found.
     * @throws SettingNotFoundException  If a required setting (e.g., tax behavior) is missing.
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponseDTO> calculate(
            @Valid @RequestBody CalculationRequestDTO request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException,
            SettingNotFoundException {

        // Retrieve the authenticated customer based on the principal
        Customer customer = getAuthenticatedCustomer(principal);

        // Perform the tax and total calculation for the customer's cart
        CalculationResponseDTO response = checkoutService.calculate(request, customer);

        // Return the calculated response
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the total amount in the cart for the authenticated customer. This provides a simple summary
     * of the cart total and currency without detailed tax or shipping calculations.
     *
     * @param principal The authenticated user's principal (to identify the customer).
     * @return A ResponseEntity containing a CheckoutDTO with the total amount and currency.
     * @throws CustomerNotFoundException If the customer is not found in the system.
     * @throws CurrencyNotFoundException If the specified currency is invalid or not found.
     */
    @GetMapping("/cart-total")
    public ResponseEntity<CheckoutDTO> cartTotal(Principal principal)
            throws CustomerNotFoundException, CurrencyNotFoundException {

        // Retrieve the authenticated customer based on the principal
        Customer customer = getAuthenticatedCustomer(principal);

        // Calculate and retrieve the simple checkout (total amount and currency)
        CheckoutDTO checkoutDTO = checkoutService.getSimpleCheckout(customer);

        // Return the cart total response
        return ResponseEntity.ok(checkoutDTO);
    }

    /**
     * Creates a payment intent in Stripe for the customer based on the provided payment details.
     * The intent is used to reserve funds for the payment transaction.
     *
     * @param request   The payment request DTO containing address, amount, and currency information.
     * @param principal The authenticated user's principal (to identify the customer).
     * @return A ResponseEntity containing the client secret for the payment intent (used by Stripe).
     * @throws StripeException           If an error occurs while interacting with Stripe.
     * @throws CustomerNotFoundException If the customer is not found in the system.
     * @throws CurrencyNotFoundException If the specified currency is invalid or not found.
     * @throws AddressNotFoundException  If the provided address is invalid or not found.
     * @throws CountryNotFoundException  If the provided country is invalid or not found.
     */
    @PostMapping("/save-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @Valid @RequestBody PaymentRequestDTO request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException,
            AddressNotFoundException, CountryNotFoundException, IOException {
        RecaptchaVerificationService verificationService = new RecaptchaVerificationService();
        RecaptchaVerificationService.VerificationResult result = verificationService.verifyToken(request.getRecaptchaResponse());

        Map<String, String> response = new HashMap<>();

        try {
            verificationService.validate(result);
        } catch (RecaptchaVerificationFailed e) {
            response.put("status", "false");
            return ResponseEntity.ok(response);
        }

        // Retrieve the authenticated customer based on the principal
        Customer customer = getAuthenticatedCustomer(principal);

        // Create the payment intent using the transaction service and get the client secret
        String clientSecret = transactionService.savePaymentIntent(request, customer);


        // Return the client secret in a map to the frontend for further Stripe processing
        response.put("clientSecret", clientSecret);
        response.put("status", "true");

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a customer session in Stripe for the authenticated customer. The session allows the customer
     * to interact with Stripe for payment and billing operations.
     *
     * @param principal The authenticated user's principal (to identify the customer).
     * @return A ResponseEntity containing the client secret for the customer session.
     * @throws CustomerNotFoundException If the customer is not found in the system.
     * @throws StripeException           If an error occurs while interacting with Stripe.
     */
    @PostMapping("/create-customer-session")
    public ResponseEntity<Map<String, String>> createCustomerSession(Principal principal)
            throws CustomerNotFoundException, StripeException {

        // Retrieve the authenticated customer based on the principal
        Customer customer = getAuthenticatedCustomer(principal);

        // Create the customer session in Stripe
        CustomerSession session = checkoutService.createCustomerSession(customer);

        // Return the client secret for the customer session to the frontend
        Map<String, String> response = new HashMap<>();
        response.put("customerSessionClientSecret", session.getClientSecret());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the authenticated customer from the security principal (email).
     *
     * @param principal The authenticated user's principal containing their email.
     * @return The Customer entity corresponding to the authenticated user.
     * @throws CustomerNotFoundException If no customer is found for the provided email.
     */
    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        // Retrieve the customer using their email (contained in the security principal)
        return customerService.getByEmail(principal.getName());
    }
}
