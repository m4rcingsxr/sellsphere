package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.BasicProductDTO;
import com.sellsphere.common.entity.payload.CartItemDTO;
import com.sellsphere.payment.StripeCheckoutService;
import com.sellsphere.payment.payload.CalculationRequest;
import com.sellsphere.payment.payload.CalculationResponse;
import com.sellsphere.payment.payload.PaymentRequest;
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

    // calculation with address with specified exchange rate or in base currency if rate is not
    // defined
    @PostMapping("/calculate-all")
    public ResponseEntity<CalculationResponse> calculateWithAddress(
            @RequestBody CalculationRequest request, Principal principal)
            throws StripeException, CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        String baseCurrency = settingService.getCurrencyCode();

        if(request.getCurrencyCode() == null) {
            request.setCurrencyCode(baseCurrency);
        }

        Currency targetCurrency = currencyRepository
                .findByCode(request.getCurrencyCode())
                .orElseThrow(CurrencyNotFoundException::new);
        Currency base = currencyRepository.findByCode(baseCurrency)
                .orElseThrow(CurrencyNotFoundException::new);

        CalculationResponse.CalculationResponseBuilder responseBuilder =
                CalculationResponse.builder();

        Calculation calculation = stripeService.calculate(request, cart, base, targetCurrency);

        responseBuilder
                .amountTotal(calculation.getAmountTotal())
                .taxAmountInclusive(calculation.getTaxAmountInclusive())
                .shippingCost(calculation.getShippingCost())
                .customerDetails(calculation.getCustomerDetails())
                .currencyCode(targetCurrency.getCode())
                .unitAmount(targetCurrency.getUnitAmount())
                .currencySymbol(targetCurrency.getSymbol());


        if (baseCurrency.equals(targetCurrency.getCode())) {
            responseBuilder.cart(cart.stream().map(CartItemDTO::new).toList());
        } else {
            BigDecimal stripeExchangeFee = BigDecimal.valueOf(0.02);
            BigDecimal exchangeRate = request.getExchangeRate().multiply(
                    BigDecimal.ONE.add(stripeExchangeFee));

            BigDecimal finalRate;
            Integer scale;
            if (targetCurrency.getUnitAmount() == 1000) {
                finalRate = exchangeRate.setScale(3, RoundingMode.HALF_UP);
                scale = 3;
            } else if (targetCurrency.getUnitAmount() == 1) {
                finalRate = exchangeRate.setScale(0, RoundingMode.HALF_UP);
                scale = 0;
            } else {
                finalRate = exchangeRate.setScale(2, RoundingMode.HALF_UP);
                scale = 2;
            }

            BigDecimal subtotal = new BigDecimal(0);

            cart.forEach(cartItem -> subtotal.add(
                    BigDecimal.valueOf(cartItem.getQuantity()).multiply(
                            cartItem.getProduct().getDiscountPrice().setScale(scale,
                                                                              RoundingMode.CEILING
                            ))));


            responseBuilder.cart(cart.stream().map(cartItem -> CartItemDTO.builder()
                    .quantity(cartItem.getQuantity())
                    .subtotal(subtotal).product(
                            BasicProductDTO.builder().alias(
                                    cartItem.getProduct().getAlias()).brandName(
                                    cartItem.getProduct().getBrand().getName()).categoryName(
                                    cartItem.getProduct().getCategory().getName()).name(
                                    cartItem.getProduct().getName()).price(
                                    cartItem.getProduct().getPrice().multiply(
                                            finalRate)).discountPrice(
                                    cartItem.getProduct().getDiscountPrice().multiply(
                                            finalRate).setScale(2, RoundingMode.CEILING)).inStock(
                                    cartItem.getProduct().isInStock()).mainImagePath(
                                    cartItem.getProduct().getMainImagePath()).discountPercent(
                                    cartItem.getProduct().getDiscountPercent()).build()).build()).toList());
        }

        return ResponseEntity.ok(responseBuilder.build());
    }

    /**
     * Rounds an amount based on the unit amount of the currency.
     * If the unit amount is 1000 (three-decimal currency), rounds to the nearest ten.
     * If the unit amount is 1 (zero-decimal currency), rounds to the nearest whole number.
     * Otherwise, rounds to the nearest integer.
     * @param amount - The amount to be rounded.
     * @param unitAmount - The unit amount of the currency.
     * @returns The rounded amount.
     */
    private long roundAmount(BigDecimal amount, long unitAmount) {
        if (unitAmount == 1000) {
            return amount.setScale(3, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(10)).setScale(0, RoundingMode.HALF_UP).longValue();
        } else if (unitAmount == 1) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        } else {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        }
    }

    // calculate without products with base currency
    @PostMapping("/calculate-total")
    public ResponseEntity<CalculationResponse> calculate(Principal principal)
            throws CustomerNotFoundException, CurrencyNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        String baseCurrencyCode = settingService.getCurrencyCode();
        Currency currency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(CurrencyNotFoundException::new);

        return ResponseEntity.ok(
                CalculationResponse.builder()
                        .amountTotal(stripeService.getAmountTotal(cart, currency))
                        .currencyCode(currency.getCode())
                        .unitAmount(currency.getUnitAmount())
                        .currencySymbol(currency.getSymbol())
                        .cart(cart.stream().map(CartItemDTO::new).toList())
                        .build()
        );
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestBody PaymentRequest request)
            throws StripeException {
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(request.getAmountTotal(),
                                                                        request.getCurrencyCode()
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
