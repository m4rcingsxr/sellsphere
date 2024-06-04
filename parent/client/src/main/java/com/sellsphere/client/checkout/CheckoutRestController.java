package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.payment.PaymentRequest;
import com.sellsphere.payment.StripeCheckoutService;
import com.sellsphere.payment.payload.CalculationRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Calculation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // calculate for products and address
    @PostMapping("/calculate")
    public ResponseEntity<CheckoutResponse> calculateWithAddress(
            @RequestBody CalculationRequest request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        String baseCurrencyCode = settingService.getCurrencyCode();

        String currencyCode = baseCurrencyCode;

        if (request != null && request.getCurrencyCode() != null) {
            currencyCode = request.getCurrencyCode();
        }

        Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow(
                CurrencyNotFoundException::new);

        var checkoutResponseBuilder = CheckoutResponse.builder();

        if (request != null) {
            Calculation calculation = stripeService.calculateTax(cart, request.getAddress(),
                                                                 request.getShippingCost(),
                                                                 baseCurrencyCode, currency, request.getExchangeRate()
            );

            // handle 3 decimal currencies 5.124 KWD -> 5124 -> 5200/5300
            if (currency.getUnitAmount() == 1000) {
                BigDecimal value = BigDecimal.valueOf(calculation.getAmountTotal());
                BigDecimal hundred = BigDecimal.valueOf(100);
                BigDecimal divided = value.divide(hundred, 0, RoundingMode.HALF_UP);
                calculation.setAmountTotal(divided.multiply(hundred).longValue());
            }

            checkoutResponseBuilder
                    .amountTotal(calculation.getAmountTotal())
                    .taxAmountInclusive(calculation.getTaxAmountInclusive())
                    .shippingCost(calculation.getShippingCost())
                    .customerDetails(calculation.getCustomerDetails());
        } else {
            checkoutResponseBuilder.amountTotal(stripeService.getAmountTotal(cart, currency));
        }

        checkoutResponseBuilder.currencyCode(currency.getCode());
        checkoutResponseBuilder.unitAmount(currency.getUnitAmount());
        checkoutResponseBuilder.currencySymbol(currency.getSymbol());
        checkoutResponseBuilder.cart(
                cart.stream().map(CartItemDto::new).toList()
        );

        return ResponseEntity.ok(checkoutResponseBuilder.build());
    }

    // calculate only for the products - for base currency code
    @PostMapping("/calculate-basic")
    public ResponseEntity<CheckoutResponse> calculate(Principal principal)
            throws CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        String baseCurrencyCode = settingService.getCurrencyCode();
        Currency currency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(CurrencyNotFoundException::new);

        return ResponseEntity.ok(
                CheckoutResponse.builder()
                        .amountTotal(stripeService.getAmountTotal(cart, currency))
                        .currencyCode(currency.getCode())
                        .unitAmount(currency.getUnitAmount())
                        .currencySymbol(currency.getSymbol())
                        .cart(cart.stream().map(CartItemDto::new).toList())
                        .build()
        );
    }


    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestBody PaymentRequest request)
            throws StripeException {
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(request.getAmountTotal(),
                                                                        request.getCurrencyCode(),
                                                                        request.getCustomerDetails()
        );

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
