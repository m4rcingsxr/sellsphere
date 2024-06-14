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

    // required
    private Address address;

    // required
    private BigDecimal shippingCost;

    // required
    private String currencyCode;

    // required on different code that base
    private BigDecimal exchangeRate;

    private String fullName;

}
