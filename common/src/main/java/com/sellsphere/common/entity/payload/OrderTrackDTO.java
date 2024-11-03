package com.sellsphere.common.entity.payload;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTrackDTO {

    private Integer id;
    private Integer orderId;
    private String status;
    private String note;
    private LocalDate updatedTime;

}
