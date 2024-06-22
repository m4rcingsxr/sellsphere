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

    private String courierId;
    private String courierName;
    private String courierLogoUrl;
    private Integer maxDeliveryTime;
    private Integer minDeliveryTime;

    private long amountTotal;
    private long amountTax;
    private long shippingAmount;
    private long shippingTax;
}
