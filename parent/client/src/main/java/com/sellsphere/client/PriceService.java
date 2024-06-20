package com.sellsphere.client;

import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
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

    private BigDecimal convertAmount(BigDecimal itemSubtotal, BigDecimal unitAmount, BigDecimal exchangeRate) {
        return itemSubtotal.multiply(exchangeRate).multiply(unitAmount);
    }

    public BigDecimal convertToDisplayAmount(long amount, String currencyCode)
            throws CurrencyNotFoundException {
        Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow(
                CurrencyNotFoundException::new);
        BigDecimal unitAmount = currency.getUnitAmount();

        if (unitAmount.longValue() == 1000L) {
            return BigDecimal.valueOf(amount).divide(unitAmount).setScale(3, RoundingMode.HALF_UP);
        } else if (unitAmount.longValue() == 100L) {
            return BigDecimal.valueOf(amount).divide(unitAmount).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(amount).divide(unitAmount).setScale(0, RoundingMode.HALF_UP);
        }

    }

    public long handleRoundingAmountByCurrency(BigDecimal amount, BigDecimal unitAmount) {

        if (unitAmount.longValue() == 1000L) {
            // For three-decimal currencies, round to the nearest ten
            BigDecimal roundedAmount = amount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN);
            return roundedAmount.longValue();
        } else if (unitAmount.longValue() == 100L) {
            // For two-decimal currencies, convert to smallest unit
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        } else {
            // For zero-decimal currencies, convert to smallest unit
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        }
    }

    // any floating point format
    public BigDecimal roundDisplayPrice(BigDecimal displayPrice, String currencyCode)
            throws CurrencyNotFoundException {
        Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow(
                CurrencyNotFoundException::new);

        long unitAmount = currency.getUnitAmount().longValue();

        if (unitAmount == 1000) {
            return displayPrice.round(new MathContext(3, RoundingMode.HALF_UP));
        } else if (unitAmount == 100) {
            return displayPrice.round(new MathContext(2, RoundingMode.HALF_UP));
        } else {
            return displayPrice.round(new MathContext(0, RoundingMode.HALF_UP));
        }
    }

    public BigDecimal convertPrice(BigDecimal price, long unitAmount, BigDecimal exchangeRate, String base, String target) {

        if(exchangeRate == null || target.equalsIgnoreCase(base)) exchangeRate = BigDecimal.ONE;

        if (unitAmount == 1000L) {
            return price.multiply(exchangeRate).setScale(3, RoundingMode.HALF_UP);
        } else if(unitAmount == 100L) {
            return price.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            return price.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
        }
    }
}
