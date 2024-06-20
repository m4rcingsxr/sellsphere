package com.sellsphere.common.entity.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateRequestDTO {

    private AddressDTO address;

    private String currencyCode;

}
