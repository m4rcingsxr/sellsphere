package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import com.sellsphere.common.entity.Address;
import com.sellsphere.easyship.payload.Rate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    @SerializedName("shipment")
    private Shipment shipment;

    @SerializedName("meta")
    private Meta meta;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {

        private List<String> errors;

        @SerializedName("request_id")
        private String requestId;

        private String status;

        @SerializedName("unavailable_couriers")
        private List<UnavailableCouriers> unavailableCouriers;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UnavailableCouriers {

            private String id;
            private String message;
            private String name;

        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shipment {

        @SerializedName("easyship_shipment_id")
        private String easyshipShipmentId;

        @SerializedName("buyer_regulatory_identifiers")
        private BuyerRegulatoryIdentifiers buyerRegulatoryIdentifiers;

        @SerializedName("consignee_tax_id")
        private String consigneeTaxId;

        @SerializedName("courier")
        private Courier courier;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("currency")
        private String currency;

        @SerializedName("delivery_state")
        private String deliveryState;

        @SerializedName("destination_address")
        private Address destinationAddress;

        @SerializedName("eei_reference")
        private String eeiReference;

        @SerializedName("incoterms")
        private String incoterms;

        @SerializedName("insurance")
        private Insurance insurance;

        @SerializedName("label_generated_at")
        private String labelGeneratedAt;

        @SerializedName("label_paid_at")
        private String labelPaidAt;

        // not_created pending generating generated printed failed technical_failed reported
        // voided void_failed
        @SerializedName("label_state")
        private String labelState;

        @SerializedName("metadata")
        private Map<String, Object> metadata;

        @SerializedName("order_data")
        private OrderData orderData;

        @SerializedName("origin_address")
        private Address originAddress;

        @SerializedName("parcels")
        private List<RequestParcel> parcels;

        @SerializedName("pickup_state")
        private String pickupState;

        @SerializedName("rates")
        private List<Rate> rates;

        @SerializedName("regulatory_identifiers")
        private RegulatoryIdentifiers regulatoryIdentifiers;

        @SerializedName("return")
        private boolean returnShipment;

        @SerializedName("return_address")
        private Address returnAddress;

        @SerializedName("sender_address")
        private Address senderAddress;

        @SerializedName("set_as_residential")
        private boolean setAsResidential;

        @SerializedName("shipment_state")
        private String shipmentState;

        @SerializedName("shipping_documents")
        private List<ShippingDocuments> shippingDocuments;

        @SerializedName("tracking_page_url")
        private String trackingPageUrl;

        // paid
        @SerializedName("trackings")
        private List<Object> trackings;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("warehouse_state")
        private String warehouseState;

        /**
         * Sum of the specified weights of all parcels in the shipment (parcel.actual_weight), as
         * measured on a scale in units specified by company.weight_unit.
         */
        @SerializedName("total_actual_weight")
        private BigDecimal totalActualWeight;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Courier {

            private String id;

            private String name;

        }
    }
}
