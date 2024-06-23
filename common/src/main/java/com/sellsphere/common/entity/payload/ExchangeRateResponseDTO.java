package com.sellsphere.common.entity.payload;

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
public class ExchangeRateResponseDTO {

    private String base;
    private long updated;
    private Map<String, BigDecimal> result;
    private BigDecimal fee;

}