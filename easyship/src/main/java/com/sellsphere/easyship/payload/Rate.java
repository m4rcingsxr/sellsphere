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

    @SerializedName("courier_id")
    private String courierId;

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

    @SerializedName("rates_in_origin_currency")
    private RatesInOriginCurrency ratesInOriginCurrency;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class RatesInOriginCurrency {

        @SerializedName("additional_services_surcharge")
        private double additionalServicesSurcharge;

        private String currency;

        @SerializedName("ddp_handling_fee")
        private double ddpHandlingFee;

        @SerializedName("estimated_import_duty")
        private double estimatedImportDuty;

        @SerializedName("estimated_import_tax")
        private double estimatedImportTax;

        @SerializedName("fuel_surcharge")
        private double fuelSurcharge;

        @SerializedName("import_duty_charge")
        private double importDutyCharge;

        @SerializedName("import_tax_charge")
        private double importTaxCharge;

        @SerializedName("import_tax_non_chargeable")
        private double importTaxNonChargeable;

        @SerializedName("insurance_fee")
        private double insuranceFee;

        @SerializedName("minimum_pickup_fee")
        private double minimumPickupFee;

        @SerializedName("oversized_surcharge")
        private double oversizedSurcharge;

        @SerializedName("provincial_sales_tax")
        private double provincialSalesTax;

        @SerializedName("remote_area_surcharge")
        private double remoteAreaSurcharge;

        @SerializedName("residential_discounted_fee")
        private double residentialDiscountedFee;

        @SerializedName("residential_full_fee")
        private double residentialFullFee;

        @SerializedName("sales_tax")
        private double salesTax;

        @SerializedName("shipment_charge")
        private double shipmentCharge;

        @SerializedName("shipment_charge_total")
        private double shipmentChargeTotal;

        @SerializedName("total_charge")
        private double totalCharge;

        @SerializedName("warehouse_handling_fee")
        private double warehouseHandlingFee;
    }
}