package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.apache.xmlbeans.impl.store.Cur;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.sellsphere.common.entity.Currency}
 */
@Value
public class CurrencyDto implements Serializable {
    Integer id;
    @NotNull(message = "Currency name is required")
    @Size(message = "Currency name must be less than or equal to 128 characters", max = 128)
    String name;
    @NotNull(message = "Currency symbol is mandatory")
    @Size(message = "Currency symbol must be less than or equal to 8 characters", max = 8)
    String symbol;
    @NotNull(message = "Currency code is mandatory")
    String code;
    @NotNull(message = "Unit amount cannot be null")
    BigDecimal unitAmount;

    public CurrencyDto(Currency other) {
        this.id = other.getId();
        this.name = other.getName();
        this.symbol = other.getSymbol();
        this.code = other.getCode();
        this.unitAmount = other.getUnitAmount();
    }
}