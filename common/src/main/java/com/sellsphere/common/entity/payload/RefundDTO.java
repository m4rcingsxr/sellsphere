package com.sellsphere.common.entity.payload;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDTO {

    private Integer paymentIntent;

    // real price - not stripe amount
    private BigDecimal amount;
    private String reason;

}
