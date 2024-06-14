package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parcel in a shipment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestParcel {

    private List<ParcelProductItemCreate> items; // List of items in the parcel

    /**
     * Total weight of the shipment, including the box weight. If you provide the total weight of
     * the shipment, then the weight for items can be optional.
     */
    @SerializedName("total_actual_weight")
    private BigDecimal totalActualWeight;

    public void addItem(ParcelProductItemCreate item) {
        if(items == null) {
            items = new ArrayList<>();
        }

        items.add(item);
    }

}