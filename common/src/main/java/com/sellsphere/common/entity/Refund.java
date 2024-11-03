package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refunds")
public class Refund extends IdentifiedEntity {

    @Column(name = "stripe_id", nullable = false)
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;

    @Column(name = "object")
    private String object;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @OneToOne
    @JoinColumn(name = "balance_transaction_id")
    private BalanceTransaction balanceTransaction;

    @Column(name = "created", nullable = false)
    private Long created;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "payment_intent_id", nullable = false)
    private PaymentIntent paymentIntent;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Transient
    public BigDecimal getDisplayAmount() {
        long unitAmount = currency.getUnitAmount().longValue();

        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(unitAmount))
                .setScale(2, RoundingMode.CEILING
        );
    }

    @Transient
    public String displayAmountString() {
        return MoneyUtil.formatAmount(getDisplayAmount(), currency.getCode());
    }

}