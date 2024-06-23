package com.sellsphere.common.entity.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequestDTO {

    // required
    @NotNull(message = "Address cannot be null")
    private AddressDTO address;

    // required
    @NotNull(message = "Shipping cost cannot be null")
    private BigDecimal shippingCost;

    // nullable - base currency
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode;

    // nullable - must be present if currency code differ from base
    private BigDecimal exchangeRate;
}