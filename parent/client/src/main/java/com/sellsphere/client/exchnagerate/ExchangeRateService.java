package com.sellsphere.client.exchnagerate;

import com.sellsphere.client.PriceService;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.ExchangeRateNotFoundException;
import com.sellsphere.common.entity.payload.ExchangeRateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling currency exchange operations.
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final String apiKey = System.getenv("FASTFOREX_API_KEY");
    private final PriceService priceService;
    private final BigDecimal stripeExchangeFee = BigDecimal.valueOf(0.02);
    private final RestTemplate restTemplate;

    /**
     * Retrieves the exchange rate and calculates the final price with fees.
     *
     * @param baseCode The base currency code.
     * @param targetCode The target currency code.
     * @param basePrice The base price.
     * @return The exchange rate response.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    public ExchangeRateResponseDTO getExchangeRate(String baseCode, String targetCode, String basePrice)
            throws CurrencyNotFoundException, ExchangeRateNotFoundException {
        ExchangeRateResponseDTO response;
            response = fetchExchangeRate(baseCode, targetCode, basePrice);

        BigDecimal rate = getExchangeRate(response);

        BigDecimal finalRate = calculateFinalRate(rate);
        Map<String, BigDecimal> updatedResult = updateExchangeResult(response, targetCode, finalRate);

        BigDecimal finalPrice = calculateFinalPrice(response, targetCode);
        updatedResult.put(targetCode.toUpperCase(), finalPrice);

        updateResponse(response, updatedResult);

        return response;
    }

    private ExchangeRateResponseDTO fetchExchangeRate(String baseCode, String targetCode, String basePrice) {
        String url = String.format(
                "https://api.fastforex.io/convert?from=%s&to=%s&amount=%s&api_key=%s",
                baseCode.toUpperCase(), targetCode.toUpperCase(), basePrice, apiKey
        );

        return restTemplate.getForObject(url, ExchangeRateResponseDTO.class);
    }

    private BigDecimal getExchangeRate(ExchangeRateResponseDTO response)
            throws ExchangeRateNotFoundException {
        if (response == null || response.getResult() == null || !response.getResult().containsKey("rate")) {
            throw new ExchangeRateNotFoundException("Exchange rate not found");
        }
        return response.getResult().get("rate");
    }

    private BigDecimal calculateFinalRate(BigDecimal rate) {
        return rate.multiply(BigDecimal.ONE.add(stripeExchangeFee)).setScale(5, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> updateExchangeResult(ExchangeRateResponseDTO response, String targetCode, BigDecimal finalRate) {
        Map<String, BigDecimal> updatedResult = new HashMap<>(response.getResult());
        updatedResult.put("rate", finalRate);
        return updatedResult;
    }

    private BigDecimal calculateFinalPrice(ExchangeRateResponseDTO response, String targetCode)
            throws ExchangeRateNotFoundException, CurrencyNotFoundException {
        BigDecimal targetAmount = response.getResult().get(targetCode.toUpperCase());
        if (targetAmount == null) {
            throw new ExchangeRateNotFoundException("Target currency not found in response");
        }
        return priceService.roundDisplayPrice(targetAmount, targetCode);
    }

    private void updateResponse(ExchangeRateResponseDTO response, Map<String, BigDecimal> updatedResult) {
        response.setUpdated(Instant.now().toEpochMilli());
        response.setFee(stripeExchangeFee);
        response.setResult(updatedResult);
    }
}
