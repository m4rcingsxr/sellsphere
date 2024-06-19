package com.sellsphere.admin.transaction;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDTO {

    private Integer paymentIntent;
    private String refundStatus;
    private BigDecimal amount;
    private String reason;

}
