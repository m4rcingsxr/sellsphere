package com.sellsphere.admin;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class PriceUtil {

    public static BigDecimal convertToDisplayPrice(Long amount, BigDecimal unitAmount) {

        BigDecimal price = BigDecimal.valueOf(amount);
        if(unitAmount.longValue() == 1000L) {
            return price.divide(unitAmount).setScale(3, RoundingMode.HALF_UP);
        } else if(unitAmount.longValue() == 100L) {
            return price.divide(unitAmount).setScale(2, RoundingMode.HALF_UP);
        } else {
            return price.divide(unitAmount).setScale(0, RoundingMode.HALF_UP);
        }
    }

    public long convertToStripeAmount(BigDecimal amount, BigDecimal unitAmount) {
        amount = amount.multiply(unitAmount);
        return handleRoundingAmountByCurrency(amount, unitAmount);
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

}
