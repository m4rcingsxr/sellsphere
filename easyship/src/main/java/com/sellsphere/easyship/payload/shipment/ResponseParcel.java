package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

/**
 * Represents a parcel in a shipment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseParcel {

    private String id;
    private List<Product> items; // List of items in the parcel

    @SerializedName("total_actual_weight")
    private double totalActualWeight;

}