package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Easyship integration
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @Column(name = "tracking_page_url", nullable = false)
    private String trackingPageUrl;

    @Column(name = "delivery_state", nullable = false)
    private String deliveryState;

    // destination address - as common Address, return address

    @Column(name = "pickup_state", nullable = false)
    private String pickupState;

    @Column(name = "return_shipment", nullable = false)
    private boolean returnShipment;

    @Column(name = "label_state", nullable = false)
    private String labelState;

    @Column(name = "label_generated_at")
    private String labelGeneratedAt;

    @Column(name = "label_pai_at")
    private String labelPaidAt;

}
