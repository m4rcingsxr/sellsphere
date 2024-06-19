package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "object")
    private String object;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "balance_transaction")
    private String balanceTransaction;

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

}