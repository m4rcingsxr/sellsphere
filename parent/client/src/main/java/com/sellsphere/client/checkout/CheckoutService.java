package com.sellsphere.client.checkout;

import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.BasicProductDTO;
import com.sellsphere.common.entity.payload.CartItemDTO;
import com.sellsphere.payment.CheckoutUtil;
import com.sellsphere.payment.Constants;
import com.sellsphere.payment.StripeCheckoutService;
import com.sellsphere.payment.payload.CalculationRequest;
import com.sellsphere.payment.payload.CalculationResponse;
import com.sellsphere.payment.payload.PaymentRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Calculation;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for handling checkout-related operations.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final StripeCheckoutService stripeService;
    private final ShoppingCartService cartService;
    private final SettingService settingService;
    private final CurrencyRepository currencyRepository;
    private final SettingRepository settingRepository;
    private final CountryRepository countryRepository;

    /**
     * Calculates the total cost with address-specific details, using a specified exchange rate
     * or the base currency if the rate is not defined.
     *
     * @param request  The calculation request containing cart and address details.
     * @param customer The customer entity.
     * @return The calculation response containing total amounts and details.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     * @throws StripeException           if there is an error with Stripe operations.
     */
    public CalculationResponse calculateWithAddress(CalculationRequest request, Customer customer)
            throws CurrencyNotFoundException, StripeException {
        List<CartItem> cart = cartService.findCartItemsByCustomer(customer);

        String baseCurrency = settingService.getCurrencyCode();

        if (request.getCurrencyCode() == null) {
            request.setCurrencyCode(baseCurrency);
        }

        Currency targetCurrency = currencyRepository
                .findByCode(request.getCurrencyCode())
                .orElseThrow(CurrencyNotFoundException::new);
        Currency base = currencyRepository.findByCode(baseCurrency)
                .orElseThrow(CurrencyNotFoundException::new);
        Calculation calculation = stripeService.calculate(request, cart, base, targetCurrency);
        // drut:
        calculation.getCustomerDetails().getAddress().setPostalCode(request.getAddress().getPostalCode());

        var responseBuilder = CalculationResponse.builder();

        responseBuilder
                .amountTotal(calculation.getAmountTotal())
                .taxAmountInclusive(calculation.getTaxAmountInclusive())
                .shippingCost(calculation.getShippingCost())
                .customerDetails(calculation.getCustomerDetails())
                .currencyCode(targetCurrency.getCode())
                .unitAmount(targetCurrency.getUnitAmount())
                .email(customer.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .currencySymbol(targetCurrency.getSymbol());

        responseBuilder.cart(cart.stream().map(item -> this.buildCartItem(
                request,
                item,
                base,
                targetCurrency
        )).toList());

        return responseBuilder.build();
    }

    /**
     * Builds a cart item DTO with currency conversion if necessary.
     *
     * @param request        The calculation request containing exchange rate details.
     * @param cartItem       The cart item entity.
     * @param baseCurrency   The base currency entity.
     * @param targetCurrency The target currency entity.
     * @return The cart item DTO.
     */
    public CartItemDTO buildCartItem(CalculationRequest request, CartItem cartItem,
                                     Currency baseCurrency, Currency targetCurrency) {
        BigDecimal providedExchangeRate = request.getExchangeRate();
        BigDecimal exchangeRate = providedExchangeRate != null ?
                providedExchangeRate.multiply(
                        BigDecimal.ONE.add(Constants.CONVERT_CURRENCY_FEE)) : null;

        if (baseCurrency.getCode().equals(targetCurrency.getCode())) {
            return new CartItemDTO(cartItem);
        } else {

            BigDecimal convertedPrice = CheckoutUtil.convertAndRoundPrice(
                    cartItem.getProduct().getDiscountPrice(), exchangeRate,
                    targetCurrency.getUnitAmount()
            );

            BigDecimal subtotal = CheckoutUtil.convertAndRoundPrice(
                    cartItem.getSubtotal(), exchangeRate, targetCurrency.getUnitAmount());

            return CartItemDTO.builder()
                    .quantity(cartItem.getQuantity())
                    .subtotal(subtotal)
                    .product(BasicProductDTO.builder()
                                     .alias(cartItem.getProduct().getAlias())
                                     .brandName(cartItem.getProduct().getBrand().getName())
                                     .categoryName(cartItem.getProduct().getCategory().getName())
                                     .name(cartItem.getProduct().getName())
                                     .discountPrice(convertedPrice)
                                     .inStock(cartItem.getProduct().isInStock())
                                     .mainImagePath(cartItem.getProduct().getMainImagePath())
                                     .discountPercent(cartItem.getProduct().getDiscountPercent())
                                     .build())
                    .build();
        }
    }

    /**
     * Calculates the total cost in base currency without considering products.
     *
     * @param customer The customer entity.
     * @return The calculation response containing total amounts.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     */
    public CalculationResponse calculateTotal(Customer customer) throws CurrencyNotFoundException {
        List<CartItem> cart = cartService.findCartItemsByCustomer(customer);

        String baseCurrencyCode = settingService.getCurrencyCode();
        Currency currency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(CurrencyNotFoundException::new);

        return CalculationResponse.builder()
                .amountTotal(stripeService.getAmountTotal(cart, currency))
                .currencyCode(currency.getCode())
                .unitAmount(currency.getUnitAmount())
                .currencySymbol(currency.getSymbol())
                .cart(cart.stream().map(CartItemDTO::new).toList())
                .email(customer.getEmail())
                .build();
    }

    /**
     * Creates a checkout session for the authenticated customer.
     *
     * @param customer The customer entity.
     * @return The created Stripe checkout session.
     * @throws StripeException          if there is an error with Stripe operations.
     * @throws SettingNotFoundException if the required setting is not found.
     */
    public Session createSession(Customer customer) throws StripeException,
            SettingNotFoundException {
        List<CartItem> cart = cartService.findCartItemsByCustomer(customer);

        Setting setting = settingRepository.findById("SUPPORTED_COUNTRY").orElseThrow(
                SettingNotFoundException::new);
        List<Integer> supportedCountryIds = Arrays.stream(setting.getValue().split(",")).map(
                Integer::valueOf).toList();
        List<Country> supportedCountries = countryRepository.findAllById(supportedCountryIds);

        return stripeService.createCheckoutSession(cart, supportedCountries);
    }

    /**
     * Creates a payment intent for the given payment request.
     *
     * @param request The payment request containing amount and currency details.
     * @return The created Stripe payment intent.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public PaymentIntent savePaymentIntent(PaymentRequest request, Customer customer)
            throws StripeException, ShoppingCartNotFoundException {
        ShoppingCart cart = cartService.findByCustomer(customer);
        if (cart.getPaymentIntentId() == null) {
            var address = request.getCustomerDetails().getAddress();

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    PaymentIntentCreateParams.Shipping.builder()
                            .setPhone(request.getPhoneNumber())
                            .setName(request.getMetadata().get("recipient_name"))
                            .setAddress(PaymentIntentCreateParams.Shipping.Address.builder()
                            .setCity(address.getCity())
                            .setState(address.getState())
                            .setLine1(address.getLine1())
                            .setLine2(address.getLine2())
                            .setCountry(address.getCountry())
                            .setPostalCode(address.getPostalCode())
                            .build()).build(),
                    request.getAmountTotal(),
                    request.getCurrencyCode(),
                    request.getMetadata().get("courier_id"),
                    request.getMetadata().get("email"),
                    request.getMetadata().get("addressIdx")
            );



            cart.setPaymentIntentId(paymentIntent.getId());
            cartService.save(cart);

            return paymentIntent;
        } else {
            PaymentIntent paymentIntent = stripeService
                    .retrievePaymentIntent(cart.getPaymentIntentId());
            return stripeService.updatePaymentIntent(paymentIntent, request);
        }


    }

}
