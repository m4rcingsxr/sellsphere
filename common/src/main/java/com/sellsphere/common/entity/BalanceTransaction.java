package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceTransaction extends IdentifiedEntity {

    @Column(name = "stripe_id")
    private String stripeId;

    private Long amount;

    private Long created;

    private Long fee;

    private String currency;

    private Long net;

}
