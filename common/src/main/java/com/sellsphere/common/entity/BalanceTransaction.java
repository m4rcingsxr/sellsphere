package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "balance_transactions")
public class BalanceTransaction extends IdentifiedEntity {

    @Column(name = "stripe_id")
    private String stripeId;

    private Long amount;

    private Long created;

    private Long fee;

    private String currency;

    private Long net;

}
