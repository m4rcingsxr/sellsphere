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

    @Column(name = "stripe_id")
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country billingCountry;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "payment_type")
    private String type;

}
