package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Represents courier selection options for a shipment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierSelection {

    @SerializedName("allow_courier_fallback")
    private boolean allowCourierFallback; // Allow fallback to the next best rate if the selected courier can't be used

    @SerializedName("apply_shipping_rules")
    private boolean applyShippingRules; // Apply any shipping rules created on the Easyship dashboard (paid only)

    @SerializedName("selected_courier_id")
    private String selectedCourierId; // ID of the selected courier

    @SerializedName("list_unavailable_couriers")
    private boolean listUnavailableCouriers; // Return detailed information with reasons for each unavailable courier
}