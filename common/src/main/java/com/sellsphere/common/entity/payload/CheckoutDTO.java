package com.sellsphere.common.entity.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutDTO {

    private long amount;
    private String currency;

}
