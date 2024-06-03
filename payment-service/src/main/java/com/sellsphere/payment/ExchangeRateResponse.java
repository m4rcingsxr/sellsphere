package com.sellsphere.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateResponse {

    @JsonProperty("base")
    private String base;

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("results")
    private Map<String, BigDecimal> results;

}