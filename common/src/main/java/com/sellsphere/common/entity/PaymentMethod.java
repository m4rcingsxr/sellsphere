package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends IdentifiedEntity {

    @Column(name = "stripe_id", nullable = false)
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "payment_type", nullable = false)
    private String type;

}
