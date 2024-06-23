package com.sellsphere.common.entity.payload;

import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {

    private AddressDTO address;

    private String currencyCode;

    private String courierName;
    private String courierLogoUrl;
    private Integer maxDeliveryTime;
    private Integer minDeliveryTime;

    private BigDecimal exchangeRate;

    private long amountTotal;
    private long amountTax;
    private long shippingAmount;
    private long shippingTax;
}
