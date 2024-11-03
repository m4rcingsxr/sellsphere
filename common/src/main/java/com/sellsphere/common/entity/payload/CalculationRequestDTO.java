package com.sellsphere.common.entity.payload;

import jakarta.validation.constraints.NotNull;
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

}