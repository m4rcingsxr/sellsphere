package com.sellsphere.client.currency_conversion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

// api not for production - only test purposes
@RestController
@RequiredArgsConstructor
public class ExchangeCurrencyRestController {

    private final String apiKey = System.getenv("FASTFOREX_API_KEY");

    @PostMapping("/exchange-rates/amount/{amount}/{unit_amount}/currency/{baseCode}/{targetCode}")
    public ResponseEntity<ExchangeRateResponse> exchangeRate(
            @PathVariable String baseCode, @PathVariable String targetCode, @PathVariable String amount, @PathVariable String unit_amount) {

        RestTemplate restTemplate = new RestTemplate();

        long unitAmount = Long.parseLong(unit_amount);
        BigDecimal result;
        if(unitAmount == 1000) {
            BigDecimal value = BigDecimal.valueOf(Long.parseLong(amount));
            BigDecimal hundred = BigDecimal.valueOf(100);
            result = value.divide(hundred, 0, RoundingMode.HALF_UP);
        } else {
            result = BigDecimal.valueOf(Long.parseLong(amount)).divide(BigDecimal.valueOf(unitAmount), 0, RoundingMode.HALF_UP);
        }

        // Use the new URL to get the exchange rate
        String url = String.format("https://api.fastforex.io/convert?from=%s&to=%s&amount=%s&api_key=%s",
                                   baseCode.toUpperCase(), targetCode.toUpperCase(), result.doubleValue(), apiKey);

        ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);


        assert response != null;
        assert response.getResult() != null;


        BigDecimal rate = response.getResult().get("rate");

        // Simplified:
        // Customer pays in presentment currency (does not need to pay fee for currency change)
        // Instead, they pay Stripe for currency conversion to the settlement currency
        // 2 percent fee not refunded - 2 percent of the final price
        BigDecimal stripeExchangeFee = BigDecimal.valueOf(0.02);
        BigDecimal finalRate = rate.multiply(BigDecimal.ONE.add(stripeExchangeFee)).setScale(5, RoundingMode.HALF_UP);

        Map<String, BigDecimal> updatedResult = new HashMap<>(response.getResult());
        updatedResult.put("rate", finalRate);

        // 3 decimals currencies
        BigDecimal finalPrice;
        if(unitAmount == 1000) {
            finalPrice = response.getResult().get(targetCode).setScale(3, RoundingMode.HALF_UP);
        } else {
            finalPrice = response.getResult().get(targetCode).setScale(2, RoundingMode.HALF_UP);

        }

        updatedResult.put(targetCode, finalPrice);

        response.setFee(stripeExchangeFee);
        response.setResult(updatedResult);

        return ResponseEntity.ok(response);
    }

}
