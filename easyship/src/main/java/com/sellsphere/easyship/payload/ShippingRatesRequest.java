package com.sellsphere.easyship.payload;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRatesRequest {

    @SerializedName("courier_selection")
    private CourierSelection courierSelection;

    @SerializedName("destination_address")
    private Address destinationAddress;

    @SerializedName("origin_address")
    private Address originAddress;

    @SerializedName("incoterms")
    private String incoterms;

    @SerializedName("insurance")
    private Insurance insurance;

    @SerializedName("parcels")
    private List<Parcel> parcels;

    @SerializedName("shipping_settings")
    private ShippingSettings shippingSettings;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingSettings {

        @SerializedName("units")
        private Units units;

        @SerializedName("output_currency")
        private String outputCurrency;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourierSelection {



        @SerializedName("apply_shipping_rules")
        private boolean applyShippingRules;


        @SerializedName("show_courier_logo_url")
        private boolean showCourierLogoUrl;

    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Units {

        @SerializedName("dimensions")
        private String dimensions;

        @SerializedName("weight")
        private String weight;


    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parcel {

        @SerializedName("items")
        private List<Item> items;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("actual_weight")
        private BigDecimal actualWeight;

        @SerializedName("declared_currency")
        private String declaredCurrency;

        @SerializedName("declared_customs_value")
        private BigDecimal declaredCustomsValue;

        @SerializedName("dimensions")
        private Dimensions dimensions;

        @SerializedName("hs_code")
        private int hsCode;

        @Builder
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Dimensions {

            @SerializedName("length")
            private BigDecimal length;

            @SerializedName("width")
            private BigDecimal width;

            @SerializedName("height")
            private BigDecimal height;
        }
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insurance {


        @SerializedName("is_insured")
        private boolean isInsured;

    }


}
