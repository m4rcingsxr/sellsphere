package com.sellsphere.client.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderReturnRequest {

    private Integer orderId;
    private String reason;
    private String note;

}
