package com.sellsphere.client.checkout;

import com.sellsphere.client.PriceService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.*;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.CustomerSession;
import com.stripe.model.tax.Calculation;
import com.stripe.param.tax.CalculationCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class for handling checkout-related operations.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final String SHIPPING_TAX_CODE = "txcd_92010001";

    private final StripeCheckoutService stripeService;
    private final ShoppingCartService cartService;
    private final SettingService settingService;
    private final CurrencyService currencyService;
    private final PriceService priceService;
    private final CartItemRepository cartItemRepository;

    /**
     * Calculates the total cost with address-specific details, using a specified exchange rate
     * or the base currency if the rate is not defined.
     *
     * @param request  The calculation request containing cart and address details.
     * @param customer The customer entity.
     * @return The calculation response containing total amounts and details.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     * @throws StripeException           if there is an error with Stripe operations.
     * @throws SettingNotFoundException  if the setting is not found.
     */
    public CalculationResponseDTO calculate(CalculationRequestDTO request, Customer customer)
            throws CurrencyNotFoundException, StripeException, SettingNotFoundException {
        String baseCurrency = settingService.getCurrencyCode(false);
        String taxBehavior = settingService.getTaxBehavior().getValue();

        List<CartItem> cart = cartService.findAllByCustomer(customer);
        String targetCurrency = (request.getCurrencyCode() == null) ? baseCurrency :
                request.getCurrencyCode().toLowerCase();

        CalculationCreateParams.Builder params = buildCalculationParams(
                request,
                baseCurrency,
                targetCurrency,
                taxBehavior,
                cart
        );
        Calculation calculation = stripeService.calculate(params.build());

        return prepareTaxCalculationResponse(calculation,
                                             cart,
                                             targetCurrency,
                                             baseCurrency,
                                             request.getAddress(),
                                             request.getExchangeRate()
        );
    }

    private CalculationResponseDTO prepareTaxCalculationResponse(Calculation calculation,
                                                                 List<CartItem> cart,
                                                                 String targetCurrency,
                                                                 String baseCurrency,
                                                                 AddressDTO address,
                                                                 BigDecimal exchangeRate)
            throws CurrencyNotFoundException {
        Currency currency = currencyService.getByCode(calculation.getCurrency());
        long unitAmount = currency.getUnitAmount().longValue();

        return CalculationResponseDTO.builder()
                .id(calculation.getId())
                .exchangeRate(exchangeRate)
                .amountTotal(calculation.getAmountTotal())
                .taxAmountInclusive(calculation.getTaxAmountInclusive())
                .displayAmount(priceService.convertToDisplayAmount(calculation.getAmountTotal(),
                                                                   calculation.getCurrency()
                ))
                .displayTax(priceService.convertToDisplayAmount(
                        calculation.getTaxAmountInclusive() - calculation.getShippingCost().getAmountTax(),
                        calculation.getCurrency()
                ))
                .displaySubtotal(priceService.convertToDisplayAmount(
                        (calculation.getAmountTotal() - calculation.getShippingCost().getAmount()),
                        calculation.getCurrency()
                ))
                .displayTotalTax(
                        priceService.convertToDisplayAmount((calculation.getTaxAmountInclusive()),
                                                            calculation.getCurrency()
                        ))
                .shippingCost(buildShippingCostDTO(calculation))
                .address(address)
                .currencyCode(calculation.getCurrency())
                .unitAmount(currency.getUnitAmount())
                .currencySymbol(currency.getSymbol())
                .cart(buildCartItemDTOs(cart, baseCurrency, targetCurrency, exchangeRate,
                                        unitAmount
                ))
                .build();
    }

    private CalculationCreateParams.Builder buildCalculationParams(CalculationRequestDTO request,
                                                                   String baseCurrency,
                                                                   String targetCurrency,
                                                                   String taxBehavior,
                                                                   List<CartItem> cart)
            throws CurrencyNotFoundException {
        CalculationCreateParams.Builder params = CalculationCreateParams.builder();
        params.setCurrency(targetCurrency);

        setLineItems(
                params,
                cart,
                baseCurrency,
                targetCurrency,
                request.getExchangeRate(),
                taxBehavior
        );
        setShippingCost(
                params,
                baseCurrency,
                targetCurrency,
                request.getShippingCost(),
                request.getExchangeRate(),
                taxBehavior
        );

        setCustomerDetails(params, request.getAddress());
        return params;
    }

    private CalculationResponseDTO.ShippingCostDTO buildShippingCostDTO(Calculation calculation)
            throws CurrencyNotFoundException {
        return CalculationResponseDTO.ShippingCostDTO.builder()
                .amount(calculation.getShippingCost().getAmount())
                .amountTax(calculation.getShippingCost().getAmountTax())
                .displayAmount(priceService.convertToDisplayAmount(
                        calculation.getShippingCost().getAmount(), calculation.getCurrency()))
                .displayTax(priceService.convertToDisplayAmount(
                        calculation.getShippingCost().getAmountTax(), calculation.getCurrency()))
                .build();
    }

    private List<CartItemDTO> buildCartItemDTOs(List<CartItem> cart, String baseCurrency,
                                                String targetCurrency, BigDecimal exchangeRate,
                                                long unitAmount) {
        return cart.stream().map(item -> CartItemDTO.builder()
                .quantity(item.getQuantity())
                .subtotal(priceService.convertPrice(
                        item.getSubtotal(),
                        unitAmount,
                        exchangeRate,
                        baseCurrency,
                        targetCurrency
                ))
                .product(BasicProductDTO.builder()
                                 .discountPrice(
                                         priceService.convertPrice(
                                                 item.getProduct().getDiscountPrice(),
                                                 unitAmount,
                                                 exchangeRate,
                                                 baseCurrency,
                                                 targetCurrency
                                         ))
                                 .price(
                                         priceService.convertPrice(
                                                 item.getProduct().getPrice(),
                                                 unitAmount,
                                                 exchangeRate,
                                                 baseCurrency,
                                                 targetCurrency
                                         ))
                                 .discountPercent(item.getProduct().getDiscountPercent())
                                 .mainImagePath(item.getProduct().getMainImagePath())
                                 .name(item.getProduct().getName())
                                 .inStock(item.getProduct().isInStock())
                                 .alias(item.getProduct().getAlias())
                                 .build())
                .build()).toList();
    }


    private void setCustomerDetails(CalculationCreateParams.Builder params, AddressDTO address) {
        params.setCustomerDetails(CalculationCreateParams.CustomerDetails.builder()
                                          .setAddress(
                                                  CalculationCreateParams.CustomerDetails.Address.builder()
                                                          .setCountry(address.getCountryCode())
                                                          .setCity(address.getCity())
                                                          .setLine1(address.getAddressLine1())
                                                          .setLine2(address.getAddressLine2())
                                                          .setPostalCode(address.getPostalCode())
                                                          .build())
                                          .setAddressSource(
                                                  CalculationCreateParams.CustomerDetails.AddressSource.SHIPPING)
                                          .build());
    }

    private void setShippingCost(CalculationCreateParams.Builder params, String base, String target,
                                 BigDecimal shippingCost, BigDecimal exchangeRate,
                                 String taxBehavior) throws CurrencyNotFoundException {
        long amount = priceService.convertAmount(shippingCost, base, target, exchangeRate);

        params.setShippingCost(CalculationCreateParams.ShippingCost.builder()
                                       .setTaxCode(SHIPPING_TAX_CODE)
                                       .setTaxBehavior(
                                               CalculationCreateParams.ShippingCost.TaxBehavior.valueOf(
                                                       taxBehavior))
                                       .setAmount(amount)
                                       .build());
    }

    private void setLineItems(CalculationCreateParams.Builder calculationBuilder,
                              List<CartItem> cart, String base, String target,
                              BigDecimal exchangeRate, String taxBehavior)
            throws CurrencyNotFoundException {
        for (CartItem cartItem : cart) {
            var lineItemBuilder = CalculationCreateParams.LineItem.builder();
            lineItemBuilder
                    .setTaxBehavior(CalculationCreateParams.LineItem.TaxBehavior.valueOf(taxBehavior))
                    .setReference(String.valueOf(cartItem.getProduct().getId()))
                    .setTaxCode(cartItem.getProduct().getTax().getId());

            BigDecimal itemSubtotal = cartItem.getSubtotal();

            long amount = priceService.convertAmount(itemSubtotal, base, target, exchangeRate);
            calculationBuilder.addLineItem(lineItemBuilder.setAmount(amount).build());
        }
    }

    /**
     * Creates a customer session in Stripe for the given customer.
     *
     * @param customer The customer entity.
     * @return The created CustomerSession.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public CustomerSession createCustomerSession(Customer customer) throws StripeException {
        return stripeService.createCustomerSession(customer.getStripeId());
    }

    /**
     * Retrieves a simplified checkout summary for the given customer.
     *
     * @param customer The customer entity.
     * @return The CheckoutDTO containing the total amount and currency.
     * @throws CurrencyNotFoundException if the specified currency is not found.
     */
    public CheckoutDTO getSimpleCheckout(Customer customer) throws CurrencyNotFoundException {
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        Currency currency = settingService.getCurrency();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart) {
            total = total.add(
                    priceService.convertAmount(cartItem.getSubtotal(), currency.getUnitAmount()));
        }

        return CheckoutDTO.builder()
                .amount(priceService.handleRoundingAmountByCurrency(total,
                                                                    currency.getUnitAmount()
                ))
                .currency(currency.getCode())
                .build();
    }
}

