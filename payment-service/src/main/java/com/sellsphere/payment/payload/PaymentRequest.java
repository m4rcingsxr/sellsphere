package com.sellsphere.payment.payload;

import com.stripe.model.tax.Calculation;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private long amountTotal;
    private String currencyCode;
    private Calculation.CustomerDetails customerDetails;

}
