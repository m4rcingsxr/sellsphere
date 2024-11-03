package com.sellsphere.common.entity;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

@UtilityClass
public class MoneyUtil {

    private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000L);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100L);

    /**
     * Converts the given amount to a displayable price by adjusting according to the provided unitAmount (currency smallest unit).
     * The scale is determined based on the unit amount to support different currencies' precision.
     *
     * @param amount     The amount to convert, in the smallest unit (e.g., cents).
     * @param unitAmount The unit amount of the currency (e.g., 100 for USD, 1000 for JPY).
     * @return A displayable price formatted to the appropriate number of decimal places.
     */
    public BigDecimal convertToDisplayPrice(Long amount, BigDecimal unitAmount) {
        BigDecimal price = BigDecimal.valueOf(amount);

        int scale = getScaleForUnitAmount(unitAmount);
        return price.divide(unitAmount, scale, RoundingMode.HALF_UP);
    }

    /**
     * Converts the given amount to Stripe's smallest unit (e.g., cents for USD).
     * This method adjusts for rounding based on the unit amount of the currency.
     *
     * @param amount     The amount in decimal format (e.g., $10.50).
     * @param unitAmount The unit amount of the currency (e.g., 100 for USD, 1000 for JPY).
     * @return The amount in the smallest unit used by Stripe (e.g., 1050 cents).
     */
    public long convertToStripeAmount(BigDecimal amount, BigDecimal unitAmount) {
        BigDecimal convertedAmount = amount.multiply(unitAmount);
        return handleRoundingByCurrency(convertedAmount, unitAmount);
    }

    /**
     * Handles rounding for different currencies, based on their smallest unit amount.
     * For currencies like JPY (which have no fractional units), this method ensures rounding
     * is applied correctly to avoid issues with Stripe payment amounts.
     *
     * @param amount     The amount to round.
     * @param unitAmount The unit amount of the currency (e.g., 100 for USD, 1000 for JPY).
     * @return The rounded amount in the smallest unit.
     */
    public long handleRoundingByCurrency(BigDecimal amount, BigDecimal unitAmount) {
        if (unitAmount.equals(ONE_THOUSAND)) {
            BigDecimal roundedAmount = amount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
            return roundedAmount.longValue();
        } else if (unitAmount.equals(ONE_HUNDRED)) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        } else {
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        }
    }

    /**
     * Formats the given amount with the appropriate currency code.
     *
     * @param amount   The amount to format.
     * @param currency The currency code (e.g., USD, EUR).
     * @return A formatted string representing the amount with the currency code.
     */
    public String formatAmount(BigDecimal amount, String currency) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setCurrency(java.util.Currency.getInstance(currency.toUpperCase()));
        return currencyFormat.format(amount);
    }

    /**
     * Determines the appropriate scale (number of decimal places) based on the currency's unit amount.
     *
     * @param unitAmount The unit amount of the currency (e.g., 100 for USD, 1000 for JPY).
     * @return The scale (number of decimal places) to use for displaying the currency.
     */
    private int getScaleForUnitAmount(BigDecimal unitAmount) {
        if (unitAmount.equals(ONE_THOUSAND)) {
            return 3;  // For currencies with 3 decimal places
        } else if (unitAmount.equals(ONE_HUNDRED)) {
            return 2;  // For currencies with 2 decimal places (e.g., USD)
        } else {
            return 0;  // For currencies with no fractional units (e.g., JPY)
        }
    }
}
