package com.sellsphere.client.currencyconversion;

import com.sellsphere.client.PriceService;
import com.sellsphere.common.entity.CurrencyNotFoundException;
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

// api not for production - only test purposes
@RestController
@RequiredArgsConstructor
public class ExchangeCurrencyRestController {

    private final String apiKey = System.getenv("FASTFOREX_API_KEY");
    private final PriceService priceService;

    // todo modify it to return only the exchange rate without converted price
    @PostMapping("/exchange-rates/amount/{basePrice}/currency/{baseCode}/{targetCode}")
    public ResponseEntity<ExchangeRateResponse> exchangeRate(
            @PathVariable String baseCode, @PathVariable String targetCode,
            @PathVariable String basePrice)
            throws CurrencyNotFoundException {

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(
                "https://api.fastforex.io/convert?from=%s&to=%s&amount=%s&api_key=%s",
                baseCode.toUpperCase(), targetCode.toUpperCase(), basePrice, apiKey
        );

        ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);

        assert response != null;
        assert response.getResult() != null;

        BigDecimal rate = response.getResult().get("rate");

        BigDecimal stripeExchangeFee = BigDecimal.valueOf(0.02);
        BigDecimal finalRate = rate.multiply(BigDecimal.ONE.add(stripeExchangeFee)).setScale(5,
                                                                                             RoundingMode.HALF_UP
        );

        Map<String, BigDecimal> updatedResult = new HashMap<>(response.getResult());
        updatedResult.put("rate", finalRate);

        BigDecimal finalPrice = priceService.roundDisplayPrice(
                response.getResult().get(targetCode.toUpperCase()), targetCode);


        updatedResult.put(targetCode.toUpperCase(), finalPrice);
        response.setUpdated(Instant.now().toEpochMilli());
        response.setFee(stripeExchangeFee);
        response.setResult(updatedResult);

        return ResponseEntity.ok(response);
    }

}
