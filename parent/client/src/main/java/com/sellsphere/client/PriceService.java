package com.sellsphere.client;

import com.sellsphere.client.checkout.CurrencyService;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final CurrencyService currencyService;
    private final Map<String, Currency> currencyCache = new HashMap<>();

    /**
     * Retrieves the currency information from cache or repository.
     *
     * @param currencyCode The currency code.
     * @return The Currency object.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    public Currency getCurrency(String currencyCode) throws CurrencyNotFoundException {
        Currency currency = currencyCache.get(currencyCode);
        if (currency == null) {
            currency = currencyService.getByCode(currencyCode);
            currencyCache.put(currencyCode, currency);
        }
        return currency;
    }

    /**
     * Converts an amount from baseCurrencyCode currency to targetCurrencyCode currency using the given exchange rate.
     *
     * @param amount       The amount to be converted.
     * @param baseCurrencyCode         The baseCurrencyCode currency code.
     * @return The converted amount in the smallest unit of the targetCurrencyCode currency.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    public long convertAmount(BigDecimal amount, String baseCurrencyCode)
            throws CurrencyNotFoundException {
            Currency baseCurrency = getCurrency(baseCurrencyCode);

            BigDecimal convertedAmount = convertAmount(amount, baseCurrency.getUnitAmount());
            return handleRoundingAmountByCurrency(convertedAmount, baseCurrency.getUnitAmount());
    }

    /**
     * Converts an amount by multiplying it with the unit amount of the currency.
     *
     * @param amount The amount to be converted.
     * @param unitAmount   The unit amount of the currency.
     * @return The converted amount.
     */
    public BigDecimal convertAmount(BigDecimal amount, BigDecimal unitAmount) {
        return amount.multiply(unitAmount);
    }

    /**
     * Converts an amount from the smallest unit of the currency to a displayable amount.
     *
     * @param amount       The amount in the smallest unit of the currency.
     * @param currencyCode The currency code.
     * @return The displayable amount.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    public BigDecimal convertToDisplayAmount(long amount, String currencyCode) throws CurrencyNotFoundException {
        Currency currency = getCurrency(currencyCode);
        BigDecimal unitAmount = currency.getUnitAmount();

        if (unitAmount.longValue() == 1000L) {
            return BigDecimal.valueOf(amount).divide(unitAmount, 3, RoundingMode.HALF_UP);
        } else if (unitAmount.longValue() == 100L) {
            return BigDecimal.valueOf(amount).divide(unitAmount, 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(amount).divide(unitAmount, 0, RoundingMode.HALF_UP);
        }
    }

    /**
     * Handles rounding of the converted amount based on the unit amount of the currency.
     *
     * @param amount     The amount to be rounded.
     * @param unitAmount The unit amount of the currency.
     * @return The rounded amount in the smallest unit of the currency.
     */
    public long handleRoundingAmountByCurrency(BigDecimal amount, BigDecimal unitAmount) {
        if (unitAmount.longValue() == 1000L) {

            // nearest 10
            BigDecimal roundedAmount = amount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
            return roundedAmount.longValue();
        } else if (unitAmount.longValue() == 100L) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        } else {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        }
    }

    /**
     * Rounds the display price based on the currency's unit amount.
     *
     * @param displayPrice The price to be rounded.
     * @param currencyCode The currency code.
     * @return The rounded display price.
     * @throws CurrencyNotFoundException if the currency is not found.
     */
    public BigDecimal roundDisplayPrice(BigDecimal displayPrice, String currencyCode)
            throws CurrencyNotFoundException {
        Currency currency = getCurrency(currencyCode);
        long unitAmount = currency.getUnitAmount().longValue();

        if (unitAmount == 1000) {
            return displayPrice.setScale(3, RoundingMode.HALF_UP);
        } else if (unitAmount == 100) {
            return displayPrice.setScale(2, RoundingMode.HALF_UP);
        } else {
            return displayPrice.setScale(0, RoundingMode.HALF_UP);
        }
    }

    public void clearCache() {
        this.currencyCache.clear();
    }
}
