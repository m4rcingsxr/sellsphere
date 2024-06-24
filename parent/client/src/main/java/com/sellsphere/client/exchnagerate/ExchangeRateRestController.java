package com.sellsphere.client.exchnagerate;

import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.ExchangeRateNotFoundException;
import com.sellsphere.common.entity.payload.ExchangeRateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling currency exchange operations.
 */
@RestController
@RequiredArgsConstructor
public class ExchangeRateRestController {

    private final ExchangeRateService exchangeRateService;

    /**
     * Retrieves the exchange rate and calculates the final price with fees.
     *
     * @param baseCode The base currency code.
     * @param targetCode The target currency code.
     * @param basePrice The base price.
     * @return The response entity containing the exchange rate and final price.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    @PostMapping("/exchange-rates/amount/{basePrice}/currency/{baseCode}/{targetCode}")
    public ResponseEntity<ExchangeRateResponseDTO> exchangeRate(
            @PathVariable String baseCode,
            @PathVariable String targetCode,
            @PathVariable String basePrice)
            throws CurrencyNotFoundException, ExchangeRateNotFoundException {

        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate(baseCode, targetCode, basePrice);

        return ResponseEntity.ok(response);
    }
}