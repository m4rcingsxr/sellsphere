package com.sellsphere.common.entity.payload;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalculationResponseDTO {

    private String id;
    private long amountTotal;
    private long taxAmountInclusive;

    private BigDecimal displayAmount;
    private BigDecimal displayTax;
    private BigDecimal exchangeRate; // applied exchange rate

    private AddressDTO address;
    private ShippingCostDTO shippingCost;

    private String currencyCode;
    private String currencySymbol;
    private BigDecimal unitAmount;

    private List<CartItemDTO> cart;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ShippingCostDTO {
        private long amount;
        private long amountTax;
    }

}
