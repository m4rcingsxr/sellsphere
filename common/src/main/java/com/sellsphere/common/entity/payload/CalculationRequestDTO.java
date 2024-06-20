package com.sellsphere.common.entity.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequestDTO {

    // required
    private AddressDTO address;

    // required
    private BigDecimal shippingCost;

    // nullable - base currency
    private String currencyCode;

    // nullable
    private BigDecimal exchangeRate;

}
