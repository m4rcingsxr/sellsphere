package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends IdentifiedEntity {

    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country billingCountry;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private Customer customer;

    @Column(name = "payment_type")
    private String type;

}
