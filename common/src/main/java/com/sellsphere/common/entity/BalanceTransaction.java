package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "balance_transactions")
public class BalanceTransaction extends IdentifiedEntity {

    @Column(name = "stripe_id", nullable = false)
    private String stripeId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "created", nullable = false)
    private Long created;

    @Column(name = "fee", nullable = false)
    private Long fee;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "net", nullable = false)
    private Long net;

    @Column(name = "exchange_rate", precision = 18, scale = 8)
    private BigDecimal exchangeRate;

    @Transient
    public BigDecimal getDisplayAmount() {
        long unitAmount = currency.getUnitAmount().longValue();

        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(unitAmount))
                .setScale(2, RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayFee() {
        long unitAmount = currency.getUnitAmount().longValue();

        return BigDecimal.valueOf(fee)
                .divide(BigDecimal.valueOf(unitAmount))
                .setScale(2, RoundingMode.CEILING);
    }

}
