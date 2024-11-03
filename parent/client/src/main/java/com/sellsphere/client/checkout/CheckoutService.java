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
 * Service for handling the checkout process, including tax calculation, payment intent creation,
 * and cart total calculation. It leverages Stripe's API to perform tax and payment operations.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final String SHIPPING_TAX_CODE = "txcd_92010001"; // Default tax code for shipping

    private final StripeCheckoutService stripeService;
    private final ShoppingCartService cartService;
    private final SettingService settingService;
    private final CurrencyService currencyService;
    private final PriceService priceService;
    private final CartItemRepository cartItemRepository;

    /**
     * Calculates the total cost for the cart, including tax and shipping, for a specific customer.
     * It uses Stripe to determine the tax amount based on the customer's address and cart contents.
     *
     * @param request  CalculationRequestDTO with cart and address details.
     * @param customer The customer entity for which the calculation is made.
     * @return CalculationResponseDTO containing detailed total amounts, tax breakdown, and more.
     * @throws CurrencyNotFoundException  If the currency specified is not found.
     * @throws StripeException            If an error occurs with Stripe's services.
     * @throws SettingNotFoundException   If required settings (like tax behavior) are not found.
     */
    public CalculationResponseDTO calculate(CalculationRequestDTO request, Customer customer)
            throws CurrencyNotFoundException, StripeException, SettingNotFoundException {

        // Retrieve the tax behavior and currency settings
        String taxBehavior = settingService.getTaxBehavior().getValue();
        String targetCurrency = settingService.getCurrency().getCode();

        // Get all cart items for the customer
        List<CartItem> cart = cartService.findAllByCustomer(customer);

        // Build the parameters for Stripe's tax calculation service
        CalculationCreateParams.Builder params = buildCalculationParams(
                request, targetCurrency, taxBehavior, cart
        );

        // Perform the tax calculation using Stripe
        Calculation calculation = stripeService.calculate(params.build());

        // Prepare and return the detailed response with tax, totals, and other breakdowns
        return prepareTaxCalculationResponse(calculation, cart, request.getAddress());
    }

    /**
     * Prepares the detailed response for tax calculation, including shipping costs, tax, subtotal,
     * and currency conversions.
     *
     * @param calculation The tax calculation result from Stripe.
     * @param cart        The list of items in the customer's cart.
     * @param address     The customer's shipping address.
     * @return A CalculationResponseDTO with totals and details.
     * @throws CurrencyNotFoundException If the currency code is invalid.
     */
    private CalculationResponseDTO prepareTaxCalculationResponse(Calculation calculation,
                                                                 List<CartItem> cart,
                                                                 AddressDTO address)
            throws CurrencyNotFoundException {

        Currency currency = currencyService.getByCode(calculation.getCurrency());

        // Build the response with detailed amounts and currency conversion
        return CalculationResponseDTO.builder()
                .id(calculation.getId())
                .amountTotal(calculation.getAmountTotal()) // Total amount with tax and shipping
                .taxAmountInclusive(calculation.getTaxAmountInclusive()) // Total tax included
                .displayAmount(priceService.convertToDisplayAmount(
                        calculation.getAmountTotal(), calculation.getCurrency()))
                .displayTax(priceService.convertToDisplayAmount(
                        calculation.getTaxAmountInclusive() - calculation.getShippingCost().getAmountTax(),
                        calculation.getCurrency()))
                .displaySubtotal(priceService.convertToDisplayAmount(
                        (calculation.getAmountTotal() - calculation.getShippingCost().getAmount()),
                        calculation.getCurrency()))
                .displayTotalTax(priceService.convertToDisplayAmount(
                        calculation.getTaxAmountInclusive(), calculation.getCurrency()))
                .shippingCost(buildShippingCostDTO(calculation))
                .address(address)
                .currencyCode(calculation.getCurrency())
                .unitAmount(currency.getUnitAmount())
                .currencySymbol(currency.getSymbol())
                .cart(cart.stream().map(CartItemDTO::new).toList())
                .build();
    }

    /**
     * Builds the Stripe CalculationCreateParams with line items, shipping costs, and customer details.
     *
     * @param request       The calculation request containing shipping and address details.
     * @param targetCurrency The target currency for the transaction.
     * @param taxBehavior    The tax behavior setting (e.g., inclusive or exclusive tax).
     * @param cart           The list of cart items for the customer.
     * @return A CalculationCreateParams.Builder configured with the necessary parameters.
     * @throws CurrencyNotFoundException If the currency code is invalid.
     */
    private CalculationCreateParams.Builder buildCalculationParams(CalculationRequestDTO request,
                                                                   String targetCurrency,
                                                                   String taxBehavior,
                                                                   List<CartItem> cart)
            throws CurrencyNotFoundException {
        CalculationCreateParams.Builder params = CalculationCreateParams.builder();
        params.setCurrency(targetCurrency); // Set transaction currency

        // Set line items (products in the cart)
        setLineItems(params, cart, targetCurrency, taxBehavior);

        // Set shipping cost and tax behavior
        setShippingCost(params, targetCurrency, request.getShippingCost(), taxBehavior);

        // Set customer details (address)
        setCustomerDetails(params, request.getAddress());

        return params;
    }

    /**
     * Builds the ShippingCostDTO for the response, including display amounts and taxes.
     *
     * @param calculation The tax calculation from Stripe, containing shipping cost details.
     * @return A ShippingCostDTO with shipping cost breakdown.
     * @throws CurrencyNotFoundException If the currency code is invalid.
     */
    private CalculationResponseDTO.ShippingCostDTO buildShippingCostDTO(Calculation calculation)
            throws CurrencyNotFoundException {
        return CalculationResponseDTO.ShippingCostDTO.builder()
                .amount(calculation.getShippingCost().getAmount()) // Shipping cost
                .amountTax(calculation.getShippingCost().getAmountTax()) // Tax on shipping
                .displayAmount(priceService.convertToDisplayAmount(
                        calculation.getShippingCost().getAmount(), calculation.getCurrency())) // Converted shipping cost
                .displayTax(priceService.convertToDisplayAmount(
                        calculation.getShippingCost().getAmountTax(), calculation.getCurrency())) // Converted tax on shipping
                .build();
    }

    /**
     * Sets customer details like address in the Stripe CalculationCreateParams.
     *
     * @param params  The Stripe CalculationCreateParams builder.
     * @param address The customer's shipping address.
     */
    private void setCustomerDetails(CalculationCreateParams.Builder params, AddressDTO address) {
        params.setCustomerDetails(CalculationCreateParams.CustomerDetails.builder()
                                          .setAddress(CalculationCreateParams.CustomerDetails.Address.builder()
                                                              .setCountry(address.getCountryCode())
                                                              .setCity(address.getCity())
                                                              .setLine1(address.getAddressLine1())
                                                              .setLine2(address.getAddressLine2())
                                                              .setPostalCode(address.getPostalCode())
                                                              .build())
                                          .setAddressSource(CalculationCreateParams.CustomerDetails.AddressSource.SHIPPING) // Address source as shipping
                                          .build());
    }

    /**
     * Configures shipping cost for the tax calculation, applying tax behavior and converting the amount.
     *
     * @param params       The Stripe CalculationCreateParams builder.
     * @param base         The currency base.
     * @param shippingCost The cost of shipping to be included in the calculation.
     * @param taxBehavior  The tax behavior (inclusive or exclusive).
     * @throws CurrencyNotFoundException If the currency code is invalid.
     */
    private void setShippingCost(CalculationCreateParams.Builder params, String base,
                                 BigDecimal shippingCost, String taxBehavior)
            throws CurrencyNotFoundException {
        long amount = priceService.convertAmount(shippingCost, base); // Convert shipping cost to target currency
        params.setShippingCost(CalculationCreateParams.ShippingCost.builder()
                                       .setTaxCode(SHIPPING_TAX_CODE) // Apply tax code for shipping
                                       .setTaxBehavior(CalculationCreateParams.ShippingCost.TaxBehavior.valueOf(taxBehavior)) // Set tax behavior
                                       .setAmount(amount) // Set shipping cost in the specified currency
                                       .build());
    }

    /**
     * Adds the cart's line items (products) to the Stripe CalculationCreateParams for tax calculation.
     *
     * @param calculationBuilder The Stripe CalculationCreateParams builder.
     * @param cart               The list of cart items.
     * @param base               The currency base.
     * @param taxBehavior        The tax behavior setting (inclusive/exclusive).
     * @throws CurrencyNotFoundException If the currency code is invalid.
     */
    private void setLineItems(CalculationCreateParams.Builder calculationBuilder,
                              List<CartItem> cart, String base, String taxBehavior)
            throws CurrencyNotFoundException {

        for (CartItem cartItem : cart) {
            var lineItemBuilder = CalculationCreateParams.LineItem.builder();
            lineItemBuilder
                    .setTaxBehavior(CalculationCreateParams.LineItem.TaxBehavior.valueOf(taxBehavior)) // Set tax behavior for the item
                    .setReference(String.valueOf(cartItem.getProduct().getId())) // Set product reference (e.g., product ID)
                    .setTaxCode(String.valueOf(cartItem.getProduct().getTax().getId())); // Set the tax code for the product

            BigDecimal itemSubtotal = cartItem.getSubtotal(); // Get the item subtotal (price * quantity)

            long amount = priceService.convertAmount(itemSubtotal, base); // Convert the subtotal to the target currency
            calculationBuilder.addLineItem(lineItemBuilder.setAmount(amount).build()); // Add line item with the converted amount
        }
    }

    /**
     * Creates a customer session in Stripe for a given customer, which can be used for further operations.
     *
     * @param customer The customer entity for whom the session is created.
     * @return The CustomerSession object created by Stripe.
     * @throws StripeException If any error occurs while interacting with Stripe's API.
     */
    public CustomerSession createCustomerSession(Customer customer) throws StripeException {
        return stripeService.createCustomerSession(customer.getStripeId()); // Create a session using the Stripe ID of the customer
    }

    /**
     * Retrieves a simplified checkout summary for the customer, containing the total cart amount and currency.
     *
     * @param customer The customer entity whose cart total is to be retrieved.
     * @return A CheckoutDTO with the total amount and currency information.
     * @throws CurrencyNotFoundException If the specified currency cannot be found.
     */
    public CheckoutDTO getSimpleCheckout(Customer customer) throws CurrencyNotFoundException {
        List<CartItem> cart = cartItemRepository.findByCustomer(customer); // Fetch all cart items for the customer

        Currency currency = settingService.getCurrency(); // Retrieve the default currency settings

        // Calculate the total amount by summing up the subtotal of each cart item
        BigDecimal total = cart.stream()
                .map(CartItem::getSubtotal)
                .map(subtotal -> priceService.convertAmount(subtotal, currency.getUnitAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build and return a simplified checkout summary with the rounded total amount and currency
        return CheckoutDTO.builder()
                .amount(priceService.handleRoundingAmountByCurrency(total, currency.getUnitAmount()))
                .currency(currency.getCode())
                .build();
    }
}
