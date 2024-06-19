package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "charges")
public class Charge extends IdentifiedEntity {

    @Column(name = "stripe_id")
    private String stripeId;

    @Column(name = "amount")
    private Long amount;

    private Long amountRefunded;

    @OneToOne
    @JoinColumn(name = "balance_transaction_id")
    private BalanceTransaction balanceTransaction;

    private String receiptUrl;

    private Boolean refunded;

    private String status;

    @OneToOne
    @JoinColumn(name = "payment_intent_id")
    private PaymentIntent paymentIntent;

    @OrderBy("created desc")
    @OneToMany(mappedBy = "charge")
    private List<Refund> refunds;

}
