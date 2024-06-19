package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "cards")
public class Card extends IdentifiedEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "brand")
    private String brand;

    @Column(name = "country")
    private String country;

    @Column(name = "exp_month")
    private Long expMonth;

    @Column(name = "exp_year")
    private Long expYear;

    @Column(name = "funding")
    private String funding;

    @Column(name = "last_4")
    private String last4;

    @Column(name = "created_time")
    private Long created;

    @Column(name = "cardholder_name")
    private String name;
}
