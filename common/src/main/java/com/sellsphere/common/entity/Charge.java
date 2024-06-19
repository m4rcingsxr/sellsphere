package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Charge extends IdentifiedEntity {

    @Column(name = "stripe_id")
    private String stripeId;

    @Column(name = "amount")
    private Long amount;

    private Long amountRefunded;

    private BalanceTransaction balanceTransaction;

    private String receiptUrl;

    private Boolean refunded;

    private String status;

    @ManyToOne
    @JoinColumn(name = "payment_intent_id")
    private PaymentIntent paymentIntent;

}
