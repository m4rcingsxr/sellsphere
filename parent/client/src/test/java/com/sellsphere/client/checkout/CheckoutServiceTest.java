package com.sellsphere.client.checkout;

import com.sellsphere.client.PriceService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.CalculationRequestDTO;
import com.sellsphere.common.entity.payload.CalculationResponseDTO;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.tax.Calculation;
import com.stripe.param.tax.CalculationCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.sellsphere.client.checkout.CheckoutTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private StripeCheckoutService stripeService;

    @Mock
    private ShoppingCartService cartService;

    @Mock
    private SettingService settingService;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private PriceService priceService;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    @Test
    void givenCalculationRequest_whenCalculate_thenCallMethodsWithCorrectParameters()
            throws StripeException, CurrencyNotFoundException, SettingNotFoundException {
        // Setup mocks
        String baseCurrency = "eur";
        String targetCurrency = baseCurrency;

        Setting taxBehavior = new Setting("TAX_BEHAVIOR", "INCLUSIVE", SettingCategory.PAYMENT);

        CalculationRequestDTO request = generateDummyCalculationRequestDTO();
        Customer customer = generateDummyCustomer();
        List<CartItem> cartItems = generateDummyCartItems();
        Calculation calculationMock = generateDummyCalculation();
        Currency dummyBaseCurrency = generateDummyEurCurrency();

        when(settingService.getCurrencyCode(false)).thenReturn(baseCurrency);
        when(settingService.getTaxBehavior()).thenReturn(taxBehavior);
        when(cartService.findAllByCustomer(customer)).thenReturn(cartItems);

        when(priceService.convertAmount(
                cartItems.get(0).getSubtotal(),
                baseCurrency,
                targetCurrency,
                null
        )).thenReturn(10000L);

        when(priceService.convertAmount(
                request.getShippingCost(),
                baseCurrency,
                targetCurrency,
                null // exchange rate
        )).thenReturn(1000L);

        when(stripeService.calculate(any(CalculationCreateParams.class)))
                .thenReturn(calculationMock);

        BigDecimal displayAmountTotal = BigDecimal.valueOf(110.0);
        BigDecimal displayTax = BigDecimal.valueOf(25.3);
        BigDecimal displaySubtotal = BigDecimal.valueOf(100);
        BigDecimal displayTotalTax = BigDecimal.valueOf(27.6);
        BigDecimal displayShippingAmount = BigDecimal.valueOf(10);
        BigDecimal displayShippingTax = BigDecimal.valueOf(2.3);

        when(priceService.convertToDisplayAmount(
                calculationMock.getAmountTotal(),
                calculationMock.getCurrency()
        )).thenReturn(displayAmountTotal);

        when(priceService.convertToDisplayAmount(
                calculationMock.getTaxAmountInclusive() - calculationMock.getShippingCost().getAmountTax(),
                calculationMock.getCurrency()
        )).thenReturn(displayTax);

        when(priceService.convertToDisplayAmount(
                calculationMock.getAmountTotal() - calculationMock.getShippingCost().getAmount(),
                calculationMock.getCurrency()
        )).thenReturn(displaySubtotal);

        when(priceService.convertToDisplayAmount(
                calculationMock.getTaxAmountInclusive(),
                calculationMock.getCurrency()
        )).thenReturn(displayTotalTax);

        when(currencyService.getByCode(baseCurrency)).thenReturn(dummyBaseCurrency);

        when(priceService.convertToDisplayAmount(
                calculationMock.getShippingCost().getAmount(), calculationMock.getCurrency()
        )).thenReturn(displayShippingAmount);

        when(priceService.convertToDisplayAmount(
                calculationMock.getShippingCost().getAmountTax(), calculationMock.getCurrency()
        )).thenReturn(displayShippingTax);

        CartItem item = cartItems.get(0);

        when(priceService.convertPrice(
                item.getProduct().getDiscountPrice(),
                dummyBaseCurrency.getUnitAmount().longValue(),
                null,
                baseCurrency,
                targetCurrency
        )).thenReturn(BigDecimal.valueOf(100));

        when(priceService.convertPrice(
                item.getProduct().getPrice(),
                dummyBaseCurrency.getUnitAmount().longValue(),
                null,
                baseCurrency,
                targetCurrency
        )).thenReturn(BigDecimal.valueOf(100));

        // Call the service method
        CalculationResponseDTO response = checkoutService.calculate(request, customer);

        // Verify interactions with mocked services
        verify(settingService).getCurrencyCode(false);
        verify(settingService).getTaxBehavior();
        verify(cartService).findAllByCustomer(customer);
        verify(priceService).convertAmount(cartItems.get(0).getSubtotal(), baseCurrency,
                                           targetCurrency, null
        );
        verify(priceService).convertAmount(request.getShippingCost(), baseCurrency, targetCurrency,
                                           null
        );
        verify(stripeService).calculate(any(CalculationCreateParams.class));
        verify(priceService).convertToDisplayAmount(
                calculationMock.getTaxAmountInclusive() - calculationMock.getShippingCost().getAmountTax(),
                calculationMock.getCurrency()
        );
        verify(priceService).convertToDisplayAmount(calculationMock.getAmountTotal(),
                                                    calculationMock.getCurrency()
        );
        verify(priceService).convertToDisplayAmount(
                calculationMock.getTaxAmountInclusive() - calculationMock.getShippingCost().getAmountTax(),
                calculationMock.getCurrency()
        );
        verify(priceService).convertToDisplayAmount(calculationMock.getTaxAmountInclusive(),
                                                    calculationMock.getCurrency()
        );
        verify(currencyService).getByCode(baseCurrency);

        // Verify the results
        assertNotNull(response);
        assertEquals(calculationMock.getId(), response.getId());
        assertEquals(calculationMock.getAmountTotal(), response.getAmountTotal());
        assertEquals(calculationMock.getTaxAmountInclusive(), response.getTaxAmountInclusive());
        assertEquals(displayAmountTotal, response.getDisplayAmount());
        assertEquals(displayTax, response.getDisplayTax());
        assertEquals(displaySubtotal, response.getDisplaySubtotal());
        assertEquals(displayTotalTax, response.getDisplayTotalTax());
        assertNull(response.getExchangeRate());
        assertEquals(baseCurrency , response.getCurrencyCode());

        // Verify the shipping cost DTO
        CalculationResponseDTO.ShippingCostDTO shippingCostDTO = response.getShippingCost();
        assertEquals(calculationMock.getShippingCost().getAmount(), shippingCostDTO.getAmount());
        assertEquals(calculationMock.getShippingCost().getAmountTax(),
                     shippingCostDTO.getAmountTax()
        );
        assertEquals(displayShippingAmount, shippingCostDTO.getDisplayAmount());
        assertEquals(displayShippingTax, shippingCostDTO.getDisplayTax());
    }
}