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
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "exp_month", nullable = false)
    private Long expMonth;

    @Column(name = "exp_year", nullable = false)
    private Long expYear;

    @Column(name = "funding")
    private String funding;

    @Column(name = "last_4", nullable = false)
    private String last4;

    @Column(name = "created_time", nullable = false)
    private Long created;

    @Column(name = "cardholder_name", nullable = false)
    private String name;
}
