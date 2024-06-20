package com.sellsphere.easyship.payload;

import com.sellsphere.common.entity.payload.AddressDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ShippingRatesResponse {

    private Meta meta;
    private AddressDTO address;
    private List<Rate> rates;

}