package com.sellsphere.easyship.payload.shipment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insurance {

    private boolean isInsured;
    private int insuredAmount;
    private String insuredCurrency;

}