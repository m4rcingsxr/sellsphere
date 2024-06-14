package com.sellsphere.payment.payload;

import com.stripe.model.tax.Calculation;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private long amountTotal;
    private String currencyCode;
    private Calculation.CustomerDetails customerDetails;

    private Map<String, String> metadata;

}
