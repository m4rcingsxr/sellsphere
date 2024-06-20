package com.sellsphere.client;

import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final CurrencyRepository currencyRepository;

    public long convertAmount(BigDecimal amount, String base, String target, BigDecimal exchangeRate)
            throws CurrencyNotFoundException {
        if (!base.equals(target)) {

            if (exchangeRate == null)
                throw new IllegalStateException(
                        "Exchange rate must be provided if calculation currency is different " +
                                "than base currency");

            Currency targetCurrency = currencyRepository.findByCode(target).orElseThrow(
                    CurrencyNotFoundException::new);

            BigDecimal convertedAmount = convertAmount(amount,
                                                                    targetCurrency.getUnitAmount(),
                                                                    exchangeRate
            );
            return handleRoundingAmountByCurrency(convertedAmount,
                                                  targetCurrency.getUnitAmount()
            );
        } else {
            Currency baseCurrency = currencyRepository.findByCode(base).orElseThrow(
                    CurrencyNotFoundException::new);

            BigDecimal convertedAmount = convertAmount(amount,
                                                                    baseCurrency.getUnitAmount()
            );
            return handleRoundingAmountByCurrency(convertedAmount,
                                                  baseCurrency.getUnitAmount()
            );
        }
    }

    public BigDecimal convertAmount(BigDecimal itemSubtotal, BigDecimal unitAmount) {
        return itemSubtotal.multiply(unitAmount);
    }

    private BigDecimal convertAmount(BigDecimal itemSubtotal, BigDecimal unitAmount, BigDecimal baseExchangeRate) {

        // apply conversion fee
        BigDecimal finalExchangeRate =  setConversionFee(baseExchangeRate);
        return itemSubtotal.multiply(finalExchangeRate).multiply(unitAmount);
    }

    public BigDecimal setConversionFee(BigDecimal baseExchangeRate) {
        return baseExchangeRate.multiply(BigDecimal.ONE.add(new BigDecimal("0.02")));
    }

    public BigDecimal convertToDisplayAmount(long amount, String currencyCode)
            throws CurrencyNotFoundException {
        Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow(
                CurrencyNotFoundException::new);
        BigDecimal unitAmount = currency.getUnitAmount();

        if (unitAmount.longValue() == 1000L) {
            return BigDecimal.valueOf(amount).multiply(unitAmount).setScale(3, RoundingMode.HALF_UP);
        } else if (unitAmount.longValue() == 100L) {
            return BigDecimal.valueOf(amount).multiply(unitAmount).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(amount).multiply(unitAmount).setScale(0, RoundingMode.HALF_UP);
        }

    }

    public long handleRoundingAmountByCurrency(BigDecimal amount, BigDecimal unitAmount) {

        if (unitAmount.longValue() == 1000L) {
            // For three-decimal currencies, round to the nearest ten
            BigDecimal scaledAmount = amount.multiply(BigDecimal.valueOf(1000));
            BigDecimal roundedAmount = scaledAmount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN);
            return roundedAmount.longValue();
        } else if (unitAmount.longValue() == 100L) {
            // For two-decimal currencies, convert to smallest unit
            return amount.multiply(BigDecimal.valueOf(unitAmount.longValue())).setScale(0, RoundingMode.HALF_UP).longValue();
        } else {
            // For zero-decimal currencies, convert to smallest unit
            return amount.multiply(BigDecimal.valueOf(unitAmount.longValue())).setScale(0, RoundingMode.HALF_UP).longValue();
        }
    }

    /**
     * Converts and rounds a price based on the currency unit amount.
     *
     * @param price The original price.
     * @param exchangeRate The exchange rate.
     * @param unitAmount The unit amount of the currency (1, 100, 1000).
     * @return The converted and rounded price.
     */
    public static BigDecimal convertAndRoundPrice(BigDecimal price, BigDecimal exchangeRate, long unitAmount) {
        BigDecimal convertedPrice = price.multiply(exchangeRate);
        return roundPrice(convertedPrice, unitAmount);
    }

    /**
     * Rounds a price based on the currency unit amount.
     *
     * @param price The price to be rounded.
     * @param unitAmount The unit amount of the currency (1, 100, 1000).
     * @return The rounded price.
     */
    public static BigDecimal roundPrice(BigDecimal price, long unitAmount) {
        if (unitAmount == 1000) {
            price = price.setScale(3, RoundingMode.HALF_UP);
            BigDecimal scaledAmount = price.multiply(BigDecimal.valueOf(1000));
            BigDecimal roundedAmount = scaledAmount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
            return roundedAmount.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        } else if (unitAmount == 100) {
            return price.setScale(2, RoundingMode.HALF_UP);
        } else {
            return price.setScale(0, RoundingMode.HALF_UP);
        }
    }

}
