package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.ApiService;
import com.sellsphere.easyship.payload.AddressDtoMin;
import com.sellsphere.payment.PaymentRequest;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Calculation;
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
    private final CurrencyRepository currencyRepository;
    private final ApiService apiService;

    @PostMapping("/calculate")
    public ResponseEntity<CheckoutResponse> calculate(@RequestBody(required = false) CalculateTaxRequest request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        String currencyCode = settingService.getCurrencyCode();
        Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow(CurrencyNotFoundException::new);

        var checkoutResponseBuilder = CheckoutResponse.builder();

        if(request != null) {
            Calculation calculation = stripeService.calculateTax(cart, request.getAddress(),  request.getShippingCost(), currency);
            checkoutResponseBuilder
                    .amountTotal(calculation.getAmountTotal())
                    .taxAmountInclusive(calculation.getTaxAmountInclusive())
                    .shippingCost(calculation.getShippingCost())
                    .customerDetails(calculation.getCustomerDetails());
        } else {
            checkoutResponseBuilder.amountTotal(stripeService.getAmountTotal(cart));
        }


        checkoutResponseBuilder.currencyCode(currency.getCode());
        checkoutResponseBuilder.unitAmount(currency.getUnitAmount());
        checkoutResponseBuilder.currencySymbol(currency.getSymbol());
        checkoutResponseBuilder.cart(
                cart.stream().map(CartItemDto::new).toList()
        );

        return ResponseEntity.ok(checkoutResponseBuilder.build());
    }


    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody PaymentRequest request)
            throws StripeException, CurrencyNotFoundException {
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(request.getAmountTotal(), request.getCurrencyCode(), request.getCustomerDetails());

        Map<String, String> map = new HashMap();
        map.put("clientSecret", paymentIntent.getClientSecret());

        return ResponseEntity.ok(map);
    }

    @PostMapping("/create_session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(Principal principal)
            throws CustomerNotFoundException, StripeException, CurrencyNotFoundException,
            SettingNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);

        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        Setting setting = settingRepository.findById("SUPPORTED_COUNTRY").orElseThrow(
                SettingNotFoundException::new);
        List<Integer> supportedCountryIds = Arrays.stream(setting.getValue().split(",")).map(
                Integer::valueOf).toList();
        List<Country> supportedCountries = countryRepository.findAllById(supportedCountryIds);

        Session session = stripeService.createCheckoutSession(cart, supportedCountries);

        Map<String, String> map = new HashMap();
        map.put("clientSecret",
                session.getRawJsonObject().getAsJsonPrimitive("client_secret").getAsString()
        );

        return ResponseEntity.ok(map);
    }

    @GetMapping("/session_status")
    public ResponseEntity<Map<String, String>> getSessionStatus(
            @RequestParam("session_id") String sessionId)
            throws StripeException {
        Session session = Session.retrieve(sessionId);

        Map<String, String> map = new HashMap<>();
        map.put("status", session.getRawJsonObject().getAsJsonPrimitive("status").getAsString());
        map.put("customer_email",
                session.getRawJsonObject().getAsJsonObject("customer_details").getAsJsonPrimitive(
                        "email").getAsString()
        );

        return ResponseEntity.ok(map);
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws
            CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
