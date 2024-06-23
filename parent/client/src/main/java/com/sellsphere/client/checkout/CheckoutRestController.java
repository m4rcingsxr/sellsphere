package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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
     * @throws SettingNotFoundException  if a required setting is not found.
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponseDTO> calculate(
            @Valid @RequestBody CalculationRequestDTO request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException,
            SettingNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        CalculationResponseDTO response = checkoutService.calculate(request, customer);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the total amount in the cart for the authenticated customer.
     *
     * @param principal The authenticated user's principal.
     * @return The CheckoutDTO containing the total amount and currency.
     * @throws CustomerNotFoundException if the customer is not found.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     */
    @GetMapping("/cart-total")
    public ResponseEntity<CheckoutDTO> cartTotal(Principal principal)
            throws CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        CheckoutDTO checkoutDTO = checkoutService.getSimpleCheckout(customer);

        return ResponseEntity.ok(checkoutDTO);
    }

    /**
     * Creates a payment intent for the given payment request.
     *
     * @param request   The payment request containing address, amount, and currency details.
     * @param principal The authenticated user's principal.
     * @return A map containing the client secret of the payment intent.
     * @throws StripeException           if there is an error with Stripe operations.
     * @throws CustomerNotFoundException if the customer is not found.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     * @throws AddressNotFoundException  if the specified address is not found.
     * @throws CountryNotFoundException  if the specified country is not found.
     */
    @PostMapping("/save-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @Valid @RequestBody PaymentRequestDTO request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException,
            AddressNotFoundException, CountryNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        String clientSecret = transactionService.savePaymentIntent(request, customer);

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", clientSecret);

        return ResponseEntity.ok(map);
    }

    /**
     * Creates a customer session in Stripe for the authenticated customer.
     *
     * @param principal The authenticated user's principal.
     * @return A map containing the client secret of the customer session.
     * @throws CustomerNotFoundException if the customer is not found.
     * @throws StripeException           if there is an error with Stripe operations.
     */
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