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

    @Column(name = "stripe_id")
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "charge_id")
    private Charge charge;

    @Column(name = "object")
    private String object;

    @Column(name = "amount")
    private Long amount;

    @OneToOne
    @JoinColumn(name = "balance_transaction_id")
    private BalanceTransaction balanceTransaction;

    @Column(name = "created")
    private Long created;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "payment_intent_id")
    private PaymentIntent paymentIntent;

    @Column(name = "reason")
    private String reason;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "status")
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

}