package com.sellsphere.client.checkout;

import com.sellsphere.client.PriceService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.CalculationRequestDTO;
import com.sellsphere.common.entity.payload.CalculationResponseDTO;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.tax.Calculation;
import com.stripe.param.tax.CalculationCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.sellsphere.client.checkout.CheckoutTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
class CheckoutServiceIntegrationTest {

    @Autowired
    private StripeCheckoutService stripeService;

    @Autowired
    private ShoppingCartService cartService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private PriceService priceService;

    @BeforeEach
    void setUp() {
        Mockito.reset(stripeService, cartService, settingService, currencyService);
        priceService.clearCache();
    }

    private static Stream<Object[]> settlementCurrencyData() {
        Currency eur = generateDummyEurCurrency();
        Currency kwd = generateDummyKwdCurrency();
        Currency jpy = generateDummyJpyCurrency();
        return Stream.of(
                new Object[]{eur, 11000L, 2530L, 1000L, 230L, "110.00", "23.00", "100.00", "25.30", "10.00", "2.30"},
                new Object[]{eur, 22000L, 5060L, 2000L, 460L, "220.00", "46.00", "200.00", "50.60", "20.00", "4.60"},
                new Object[]{kwd, 11000L, 2530L, 1000L, 230L, "11.000", "2.300", "10.000", "2.530", "1.000", "0.230"},
                new Object[]{kwd, 22000L, 5060L, 2000L, 460L, "22.000", "4.600", "20.000", "5.060", "2.000", "0.460"},
                new Object[]{jpy, 11000L, 2530L, 1000L, 230L, "11000", "2300", "10000", "2530", "1000", "230"},
                new Object[]{jpy, 22000L, 5060L, 2000L, 460L, "22000", "4600", "20000", "5060", "2000", "460"}
        );
    }

    @ParameterizedTest
    @MethodSource("settlementCurrencyData")
    void givenDataWithSettlementCurrency_whenCalculate_thenReturnCalculationResponse(
            Currency baseCurrency,
            long amountTotal,
            long totalTaxAmount,
            long shippingCostAmount,
            long shippingTaxAmount,
            String displayAmountTotal,
            String displayTax,
            String displaySubtotal,
            String displayTotalTax,
            String displayShippingAmount,
            String displayShippingTax
    ) throws StripeException, CurrencyNotFoundException, SettingNotFoundException {
        // Setup mocks
        String baseCurrencyCode = baseCurrency.getCode();

        Setting taxBehavior = new Setting("TAX_BEHAVIOR", "INCLUSIVE", SettingCategory.PAYMENT);

        CalculationRequestDTO request = generateDummyCalculationRequestDTO(new BigDecimal(displayShippingAmount), baseCurrencyCode, BigDecimal.ONE);

        Customer customer = generateDummyCustomer();
        List<CartItem> cartItems = generateDummyCartItems();
        Calculation calculationMock = generateDummyCalculation(amountTotal, totalTaxAmount, shippingCostAmount, shippingTaxAmount, baseCurrencyCode);

        when(settingService.getCurrencyCode(false)).thenReturn(baseCurrencyCode);
        when(settingService.getTaxBehavior()).thenReturn(taxBehavior);
        when(cartService.findAllByCustomer(customer)).thenReturn(cartItems);
        when(currencyService.getByCode(baseCurrencyCode)).thenReturn(baseCurrency);
        when(stripeService.calculate(any(CalculationCreateParams.class)))
                .thenReturn(calculationMock);

        // Call the service method
        CalculationResponseDTO response = checkoutService.calculate(request, customer);

        // Verify interactions with mocked services
        verify(settingService).getCurrencyCode(false);
        verify(settingService).getTaxBehavior();
        verify(cartService).findAllByCustomer(customer);
        verify(stripeService).calculate(any(CalculationCreateParams.class));
        verify(currencyService, times(2)).getByCode(baseCurrencyCode);

        // Verify the results
        assertNotNull(response);
        assertEquals(calculationMock.getId(), response.getId());
        assertEquals(calculationMock.getAmountTotal(), response.getAmountTotal());
        assertEquals(calculationMock.getTaxAmountInclusive(), response.getTaxAmountInclusive());
        assertEquals(new BigDecimal(displayAmountTotal), response.getDisplayAmount());
        assertEquals(new BigDecimal(displayTax), response.getDisplayTax());
        assertEquals(new BigDecimal(displaySubtotal), response.getDisplaySubtotal());
        assertEquals(new BigDecimal(displayTotalTax), response.getDisplayTotalTax());

        // Verify the shipping cost DTO
        CalculationResponseDTO.ShippingCostDTO shippingCostDTO = response.getShippingCost();
        assertEquals(calculationMock.getShippingCost().getAmount(), shippingCostDTO.getAmount());
        assertEquals(calculationMock.getShippingCost().getAmountTax(),
                     shippingCostDTO.getAmountTax()
        );
        assertEquals(new BigDecimal(displayShippingAmount), shippingCostDTO.getDisplayAmount());
        assertEquals(new BigDecimal(displayShippingTax), shippingCostDTO.getDisplayTax());
    }

}

