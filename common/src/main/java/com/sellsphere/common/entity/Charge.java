package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "charges")
public class Charge extends IdentifiedEntity {

    @Column(name = "stripe_id", nullable = false)
    private String stripeId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "amount_refunded", nullable = false)
    private Long amountRefunded;

    @OneToOne
    @JoinColumn(name = "balance_transaction_id", nullable = true)
    private BalanceTransaction balanceTransaction;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "refuned")
    private Boolean refunded;

    @Column(name = "status")
    private String status;

    @OneToOne
    @JoinColumn(name = "payment_intent_id")
    private PaymentIntent paymentIntent;

    @OrderBy("created desc")
    @OneToMany(mappedBy = "charge", cascade = CascadeType.ALL)
    private List<Refund> refunds = new ArrayList<>();

}
