package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "couriers")
public class Courier extends IdentifiedEntity {

    @Column(name = "courier_name", nullable = false, unique = true)
    private String name;

    @Column(name = "courier_logo_url")
    private String logoUrl;

    @Column(name = "min_delivery_time", nullable = false)
    private Integer minDeliveryTime;

    @Column(name = "max_delivery_time", nullable = false)
    private Integer maxDeliveryTime;
}
