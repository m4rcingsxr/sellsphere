package com.sellsphere.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CheckoutUtil {

    static {
        StripeConfig.init();
    }

    /**
     * Calculates the exchange rate with the Stripe fee included.
     *
     * @param providedExchangeRate The provided exchange rate.
     * @param unitAmount           The unit amount of the currency.
     * @return The calculated exchange rate.
     */
    public static BigDecimal calculateExchangeRate(BigDecimal providedExchangeRate, long unitAmount) {
        BigDecimal stripeExchangeFee = BigDecimal.valueOf(0.02);
        BigDecimal exchangeRate = providedExchangeRate.multiply(BigDecimal.ONE.add(stripeExchangeFee));
        if (unitAmount == 1000) {
            return exchangeRate.setScale(3, RoundingMode.HALF_UP);
        } else if (unitAmount == 1) {
            return exchangeRate.setScale(0, RoundingMode.HALF_UP);
        } else {
            return exchangeRate.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Rounds a BigDecimal amount according to the currency unit amount.
     * For three-decimal currencies, it rounds to the nearest ten with the least-significant
     * digit as 0.
     * For two-decimal currencies, it rounds to two decimal places.
     * For zero-decimal currencies, it rounds to the nearest integer.
     *
     * @param amount     The amount to be rounded.
     * @param unitAmount The unit amount of the currency (1, 100, 1000).
     * @return The rounded and multiplied by unit amount long value.
     */
    public static long roundAmount(BigDecimal amount, long unitAmount) {
        if (unitAmount == 1000) {
            // For three-decimal currencies, round to the nearest ten
            amount = amount.setScale(3, RoundingMode.HALF_UP);
            BigDecimal scaledAmount = amount.multiply(BigDecimal.valueOf(1000));
            BigDecimal roundedAmount = scaledAmount.divide(BigDecimal.TEN, 0,
                                                           RoundingMode.HALF_UP
            ).multiply(BigDecimal.TEN);
            return roundedAmount.longValue();
        } else if (unitAmount == 100) {
            // For two-decimal currencies, round to two decimal places
            return amount.multiply(BigDecimal.valueOf(unitAmount)).setScale(2, RoundingMode.HALF_UP).longValue();
        } else {
            // For zero-decimal currencies, round to the nearest integer
            return amount.multiply(BigDecimal.valueOf(unitAmount)).setScale(0, RoundingMode.HALF_UP).longValue() * unitAmount;
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
