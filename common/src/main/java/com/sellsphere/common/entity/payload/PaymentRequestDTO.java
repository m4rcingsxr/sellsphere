package com.sellsphere.common.entity.payload;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {

    private AddressDTO address;

    private String currencyCode;
    private String phoneNumber;
    private String calculationId;

    private long amountTotal;
    private String courierId;
    private String recipientEmail;

}
