package com.sellsphere.payment;

import com.stripe.model.tax.Calculation;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private long amountTotal;
    private String currencyCode;
    private Calculation.CustomerDetails customerDetails;

}
