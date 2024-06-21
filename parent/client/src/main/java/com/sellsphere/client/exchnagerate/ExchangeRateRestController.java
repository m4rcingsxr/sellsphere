package com.sellsphere.client.exchnagerate;

import com.sellsphere.client.PriceService;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.payload.ExchangeRateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling currency exchange operations.
 * Note: This API is not for production use, only for test purposes.
 */
@RestController
@RequiredArgsConstructor
public class ExchangeRateRestController {

    private final String apiKey = System.getenv("FASTFOREX_API_KEY");
    private final PriceService priceService;
    private final BigDecimal stripeExchangeFee = BigDecimal.valueOf(0.02);
    private final RestTemplate restTemplate = new RestTemplate();

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
            @PathVariable String basePrice) throws CurrencyNotFoundException {

        ExchangeRateResponseDTO response = fetchExchangeRate(baseCode, targetCode, basePrice);
        BigDecimal rate = getExchangeRate(response);

        BigDecimal finalRate = calculateFinalRate(rate);
        Map<String, BigDecimal> updatedResult = updateExchangeResult(response, targetCode, finalRate);

        BigDecimal finalPrice = calculateFinalPrice(response, targetCode);
        updatedResult.put(targetCode.toUpperCase(), finalPrice);

        updateResponse(response, updatedResult);

        return ResponseEntity.ok(response);
    }

    /**
     * Fetches the exchange rate from the external API.
     *
     * @param baseCode The base currency code.
     * @param targetCode The target currency code.
     * @param basePrice The base price.
     * @return The exchange rate response.
     */
    private ExchangeRateResponseDTO fetchExchangeRate(String baseCode, String targetCode, String basePrice) {
        String url = String.format(
                "https://api.fastforex.io/convert?from=%s&to=%s&amount=%s&api_key=%s",
                baseCode.toUpperCase(), targetCode.toUpperCase(), basePrice, apiKey
        );

        return restTemplate.getForObject(url, ExchangeRateResponseDTO.class);
    }

    /**
     * Retrieves the exchange rate from the response.
     *
     * @param response The exchange rate response.
     * @return The exchange rate.
     * @throws CurrencyNotFoundException if the exchange rate is not found.
     */
    private BigDecimal getExchangeRate(ExchangeRateResponseDTO response) throws CurrencyNotFoundException {
        if (response == null || response.getResult() == null || !response.getResult().containsKey("rate")) {
            throw new CurrencyNotFoundException("Exchange rate not found");
        }
        return response.getResult().get("rate");
    }

    /**
     * Calculates the final exchange rate including fees.
     *
     * @param rate The original exchange rate.
     * @return The final exchange rate.
     */
    private BigDecimal calculateFinalRate(BigDecimal rate) {
        return rate.multiply(BigDecimal.ONE.add(stripeExchangeFee)).setScale(5, RoundingMode.HALF_UP);
    }

    /**
     * Updates the exchange result with the final rate.
     *
     * @param response The exchange rate response.
     * @param targetCode The target currency code.
     * @param finalRate The final exchange rate.
     * @return The updated exchange result.
     */
    private Map<String, BigDecimal> updateExchangeResult(ExchangeRateResponseDTO response, String targetCode, BigDecimal finalRate) {
        Map<String, BigDecimal> updatedResult = new HashMap<>(response.getResult());
        updatedResult.put("rate", finalRate);
        return updatedResult;
    }

    /**
     * Calculates the final price in the target currency.
     *
     * @param response The exchange rate response.
     * @param targetCode The target currency code.
     * @return The final price.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    private BigDecimal calculateFinalPrice(ExchangeRateResponseDTO response, String targetCode) throws CurrencyNotFoundException {
        BigDecimal targetAmount = response.getResult().get(targetCode.toUpperCase());
        if (targetAmount == null) {
            throw new CurrencyNotFoundException("Target currency not found in response");
        }
        return priceService.roundDisplayPrice(targetAmount, targetCode);
    }

    /**
     * Updates the exchange rate response with the final results.
     *
     * @param response The exchange rate response.
     * @param updatedResult The updated exchange result.
     */
    private void updateResponse(ExchangeRateResponseDTO response, Map<String, BigDecimal> updatedResult) {
        response.setUpdated(Instant.now().toEpochMilli());
        response.setFee(stripeExchangeFee);
        response.setResult(updatedResult);
    }


}
