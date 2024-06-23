package com.sellsphere.client;

import com.sellsphere.client.checkout.CurrencyService;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private PriceService priceService;

    private Currency usdCurrency;
    private Currency jpyCurrency;
    private Currency kwdCurrency;

    @BeforeEach
    void setUp() {
        usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setUnitAmount(BigDecimal.valueOf(100));

        jpyCurrency = new Currency();
        jpyCurrency.setCode("JPY");
        jpyCurrency.setUnitAmount(BigDecimal.valueOf(1));

        kwdCurrency = new Currency();
        kwdCurrency.setCode("KWD");
        kwdCurrency.setUnitAmount(BigDecimal.valueOf(1000));
    }

    @Test
    void givenUSDCurrency_whenConvertToDisplayAmount_thenReturnCorrectAmount()
            throws CurrencyNotFoundException {
        // Given
        when(currencyService.getByCode("USD")).thenReturn(usdCurrency);

        // When
        BigDecimal result = priceService.convertToDisplayAmount(10000L, "USD");

        // Then
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result);
    }

    @Test
    void givenJPYCurrency_whenConvertToDisplayAmount_thenReturnCorrectAmount()
            throws CurrencyNotFoundException {
        // Given
        when(currencyService.getByCode("JPY")).thenReturn(jpyCurrency);

        // When
        BigDecimal result = priceService.convertToDisplayAmount(10000L, "JPY");

        // Then
        assertEquals(BigDecimal.valueOf(10000).setScale(0), result);
    }

    @Test
    void givenKWDCurrency_whenConvertToDisplayAmount_thenReturnCorrectAmount()
            throws CurrencyNotFoundException {
        // Given
        when(currencyService.getByCode("KWD")).thenReturn(kwdCurrency);

        // When
        BigDecimal result = priceService.convertToDisplayAmount(10000L, "KWD");

        // Then
        assertEquals(BigDecimal.valueOf(10.000).setScale(3), result);
    }

    @Test
    void givenDifferentCurrencies_whenConvertAmount_thenReturnCorrectConvertedAmount()
            throws CurrencyNotFoundException {
        // Given
        when(currencyService.getByCode("KWD")).thenReturn(kwdCurrency);

        // When
        long result = priceService.convertAmount(BigDecimal.valueOf(100), "USD", "KWD",
                                                 BigDecimal.valueOf(0.3)
        );

        // Then
        assertEquals(30000, result);
    }

    @Test
    void givenDifferentCurrenciesWithoutExchangeRate_whenConvertAmount_thenThrowException() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                priceService.convertAmount(BigDecimal.valueOf(100), "USD", "KWD", null));
        assertEquals(
                "Exchange rate must be provided if calculation currency is different than " +
                        "baseCurrencyCode currency",
                exception.getMessage()
        );
    }

    @Test
    void givenSameCurrency_whenConvertAmount_thenReturnSameAmount()
            throws CurrencyNotFoundException {
        // Given
        when(currencyService.getByCode("USD")).thenReturn(usdCurrency);

        // When
        long result = priceService.convertAmount(BigDecimal.valueOf(100), "USD", "USD", null);

        // Then
        assertEquals(10000, result);
    }

    @Test
    void givenUSDPrice_whenConvertPrice_thenReturnCorrectConvertedPrice() {
        // When
        BigDecimal result = priceService.convertPrice(BigDecimal.valueOf(100), 100L,
                                                      BigDecimal.valueOf(1), "USD", "USD"
        );

        // Then
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result);
    }

    @Test
    void givenJPYPrice_whenConvertPrice_thenReturnCorrectConvertedPrice() {
        // When
        BigDecimal result = priceService.convertPrice(BigDecimal.valueOf(100), 1L,
                                                      BigDecimal.valueOf(1), "JPY", "JPY"
        );

        // Then
        assertEquals(BigDecimal.valueOf(100).setScale(0), result);
    }

    @Test
    void givenKWDPrice_whenConvertPrice_thenReturnCorrectConvertedPrice() {
        // When
        BigDecimal result = priceService.convertPrice(BigDecimal.valueOf(100), 1000L,
                                                      BigDecimal.valueOf(1), "KWD", "KWD"
        );

        // Then
        assertEquals(BigDecimal.valueOf(100.000).setScale(3), result);
    }

    @Test
    void givenPriceWithoutExchangeRate_whenConvertPrice_thenUseOneAsExchangeRate() {
        // When
        BigDecimal result = priceService.convertPrice(BigDecimal.valueOf(100), 100L, null, "USD",
                                                      "USD"
        );

        // Then
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result);
    }

    @Test
    void givenPriceWithDifferentCurrenciesAndExchangeRate_whenConvertPrice_thenReturnCorrectConvertedPrice()
            throws CurrencyNotFoundException {

        // When
        BigDecimal result = priceService.convertPrice(BigDecimal.valueOf(100), 1000L,
                                                      BigDecimal.valueOf(0.3), "USD", "KWD"
        );

        // Then
        assertEquals(BigDecimal.valueOf(30.000).setScale(3), result);
    }
}
