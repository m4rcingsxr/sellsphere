package com.sellsphere.client.exchnagerate;

import com.sellsphere.client.PriceService;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.ExchangeRateNotFoundException;
import com.sellsphere.common.entity.payload.ExchangeRateResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private PriceService priceService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void givenExchangeCurrencyRequiredData_whenExchangeRate_thenShouldReturnExchangeRateAndConvertedPrice()
            throws CurrencyNotFoundException, ExchangeRateNotFoundException {
        String baseCode = "USD";
        String targetCode = "EUR";
        String basePrice = "100";

        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        Map<String, BigDecimal> resultMap = new HashMap<>();
        resultMap.put("rate", BigDecimal.valueOf(0.85));
        resultMap.put("EUR", BigDecimal.valueOf(85));
        mockResponse.setResult(resultMap);

        when(restTemplate.getForObject(anyString(), eq(ExchangeRateResponseDTO.class))).thenReturn(mockResponse);
        when(priceService.roundDisplayPrice(any(BigDecimal.class), eq(targetCode))).thenReturn(BigDecimal.valueOf(85).setScale(2, RoundingMode.HALF_UP));

        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate(baseCode, targetCode, basePrice);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(85).setScale(2, RoundingMode.HALF_UP), response.getResult().get("EUR"));
        assertEquals(BigDecimal.valueOf(0.867).setScale(5, RoundingMode.HALF_UP), response.getResult().get("rate")); // 0.85 * 1.02
        assertEquals(BigDecimal.valueOf(0.02), response.getFee());
    }

    @Test
    void givenInvalidData_whenExchangeRate_thenShouldThrowExchangeRateNotFoundException() throws CurrencyNotFoundException {
        String baseCode = "USD";
        String targetCode = "XYZ";
        String basePrice = "100";

        when(restTemplate.getForObject(anyString(), eq(ExchangeRateResponseDTO.class))).thenReturn(null);

        assertThrows(ExchangeRateNotFoundException.class, () -> exchangeRateService.getExchangeRate(baseCode, targetCode, basePrice));
    }
}