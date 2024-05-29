package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutRestController {

    private final CustomerService customerService;
    private final StripeCheckoutService stripeService;
    private final CartItemRepository cartItemRepository;

    @PostMapping("/create_session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(Principal principal)
            throws CustomerNotFoundException, StripeException {
        Customer customer = getAuthenticatedCustomer(principal);

        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        Session session = stripeService.createCheckoutSession(cart);

        Map<String, String> map = new HashMap();
        map.put("clientSecret", session.getRawJsonObject().getAsJsonPrimitive("client_secret").getAsString());

        return ResponseEntity.ok(map);
    }

    @GetMapping("/session_status")
    public ResponseEntity<Map<String, String>> getSessionStatus(@RequestParam("session_id") String sessionId)
            throws StripeException {
        Session session = Session.retrieve(sessionId);

        Map<String, String> map = new HashMap<>();
        map.put("status", session.getRawJsonObject().getAsJsonPrimitive("status").getAsString());
        map.put("customer_email", session.getRawJsonObject().getAsJsonObject("customer_details").getAsJsonPrimitive("email").getAsString());

        return ResponseEntity.ok(map);
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws
            CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
