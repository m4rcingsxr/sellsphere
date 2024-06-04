package com.sellsphere.client.currencyconversion;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateResponse {

    private String base;
    private String updated;
    private Map<String, BigDecimal> result;
    private BigDecimal fee;

}