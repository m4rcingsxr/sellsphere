package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.setting.SettingRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
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
    private final SettingService settingService;
    private final SettingRepository settingRepository;
    private final CountryRepository countryRepository;

    @PostMapping("/create_session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(Principal principal)
            throws CustomerNotFoundException, StripeException, CurrencyNotFoundException,
            SettingNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);

        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        String currencyCode = settingService.getCurrencyCode();

        Setting setting = settingRepository.findById("SUPPORTED_COUNTRY").orElseThrow(SettingNotFoundException::new);
        List<Integer> supportedCountryIds = Arrays.stream(setting.getValue().split(",")).map(
                Integer::valueOf).toList();
        List<Country> supportedCountries = countryRepository.findAllById(supportedCountryIds);

        Session session = stripeService.createCheckoutSession(cart, currencyCode, supportedCountries);

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
