package com.sellsphere.easyship.payload;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Rate {

    @SerializedName("additional_services_surcharge")
    private double additionalServicesSurcharge;

    @SerializedName("available_handover_options")
    private List<String> availableHandoverOptions;

    @SerializedName("cost_rank")
    private int costRank;

    @SerializedName("courier_logo_url")
    private String courierLogoUrl;

    @SerializedName("courier_name")
    private String courierName;

    private String currency;

    @SerializedName("delivery_time_rank")
    private int deliveryTimeRank;

    private String description;


    @SerializedName("max_delivery_time")
    private int maxDeliveryTime;

    @SerializedName("min_delivery_time")
    private int minDeliveryTime;

    @SerializedName("total_charge")
    private double totalCharge;
}
