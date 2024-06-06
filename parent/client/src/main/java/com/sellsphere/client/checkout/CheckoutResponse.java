package com.sellsphere.client.checkout;

import com.stripe.model.tax.Calculation;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutResponse {

    private long amountTotal;
    private long taxAmountInclusive;
    private Calculation.CustomerDetails customerDetails;
    private Calculation.ShippingCost shippingCost;
    private long unitAmount;
    private String currencyCode;
    private String currencySymbol;

    private List<CartItemDto> cart;
}