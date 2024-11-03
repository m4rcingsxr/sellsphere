package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Currency;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.sellsphere.common.entity.Currency}
 */
@Value
public class CurrencyDTO implements Serializable {

    Integer id;
    String name;
    String symbol;
    String code;
    BigDecimal unitAmount;

    public CurrencyDTO(Currency other) {
        this.id = other.getId();
        this.name = other.getName();
        this.symbol = other.getSymbol();
        this.code = other.getCode();
        this.unitAmount = other.getUnitAmount();
    }
}