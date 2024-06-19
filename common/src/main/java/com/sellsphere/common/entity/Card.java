package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cards")
public class Card extends IdentifiedEntity {

    @Column(name = "stripe_id")
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "brand")
    private String brand;

    @Column(name = "country")
    private String country;

    @Column(name = "exp_month")
    private Integer expMonth;

    @Column(name = "exp_year")
    private Integer expYear;

    @Column(name = "funding")
    private String funding;

    @Column(name = "last_4")
    private String last4;

    @Column(name = "created_time")
    private Long created;

    @Column(name = "cardholder_name")
    private String name;
}
