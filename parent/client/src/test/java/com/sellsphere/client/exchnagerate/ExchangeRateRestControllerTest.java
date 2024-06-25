package com.sellsphere.client.exchnagerate;

import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.ExchangeRateNotFoundException;
import com.sellsphere.common.entity.payload.ExchangeRateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {ExchangeRateRestController.class, ExchangeRateService.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExchangeRateRestControllerTest {

    // todo: mockbean does not work, method inside exchangeRateService are called
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    private ExchangeRateResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        resultMap.put("rate", BigDecimal.valueOf(0.85));
        resultMap.put("EUR", BigDecimal.valueOf(85));

        mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setBase("USD");
        mockResponse.setUpdated(System.currentTimeMillis());
        mockResponse.setResult(resultMap);
        mockResponse.setFee(BigDecimal.valueOf(0.02));
    }

    @Test
    void testExchangeRate() throws Exception {
        String baseCode = "USD";
        String targetCode = "EUR";
        String basePrice = "100";

        given(exchangeRateService.getExchangeRate(anyString(), anyString(), anyString())).willReturn(mockResponse);

        mockMvc.perform(post("/exchange-rates/amount/{basePrice}/currency/{baseCode}/{targetCode}", basePrice, baseCode, targetCode)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"base\":\"USD\",\"result\":{\"rate\":0.85,\"EUR\":85},\"fee\":0.02}"));
    }

    @Test
    void testExchangeRateCurrencyNotFoundException() throws Exception {
        String baseCode = "USD";
        String targetCode = "XYZ";
        String basePrice = "100";

        given(exchangeRateService.getExchangeRate(anyString(), anyString(), anyString())).willThrow(new CurrencyNotFoundException("Currency not found"));

        mockMvc.perform(post("/exchange-rates/amount/{basePrice}/currency/{baseCode}/{targetCode}", basePrice, baseCode, targetCode)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testExchangeRateExchangeRateNotFoundException() throws Exception {
        String baseCode = "USD";
        String targetCode = "EUR";
        String basePrice = "100";

        given(exchangeRateService.getExchangeRate(anyString(), anyString(), anyString())).willThrow(new ExchangeRateNotFoundException("Exchange rate not found"));

        mockMvc.perform(post("/exchange-rates/amount/{basePrice}/currency/{baseCode}/{targetCode}", basePrice, baseCode, targetCode)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}