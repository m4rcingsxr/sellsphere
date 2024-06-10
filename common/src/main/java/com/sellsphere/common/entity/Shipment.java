package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Easyship integration
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "shipments")
public class Shipment {

    /**
     * Easyship shipment id.
     * Fetch track, use to create label
     */
    @Id
    @Column(name = "shipment_id")
    private String shipmentId;

    /**
     * Courier id of selected rate
     */
    @Column(name = "courier_id", nullable = false)
    private String courierId;


}
