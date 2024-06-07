package com.sellsphere.payment.payload;

import com.sellsphere.common.entity.payload.CartItemDTO;
import com.stripe.model.tax.Calculation;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalculationResponse {
    private long amountTotal;
    private long taxAmountInclusive;
    private Calculation.CustomerDetails customerDetails;
    private Calculation.ShippingCost shippingCost;
    private long unitAmount;
    private String currencyCode;
    private String currencySymbol;
    private List<CartItemDTO> cart;
}
