package com.sellsphere.payment.payload;

import com.stripe.model.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {

    private Address address;
    private BigDecimal shippingCost;
    private String currencyCode;
    private BigDecimal exchangeRate;

}
