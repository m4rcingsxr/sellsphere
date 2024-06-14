package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import com.sellsphere.easyship.payload.Address;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest {

    /**
     * Buyer's identifiers for various tax, import and export regulations.
     */
    @SerializedName("buyer_regulatory_identifiers")
    private BuyerRegulatoryIdentifiers buyerRegulatoryIdentifiers;

    /**
     * The consignee's tax identification number or EIN. This is required for customs clearance
     * when shipping to certain countries, such as China and Brazil.
     */
    @SerializedName("consignee_tax_id")
    private String consigneeTaxId;

    // Courier selection options
    @SerializedName("courier_selection")
    private CourierSelection courierSelection;

    @SerializedName("destination_address")
    private Address destinationAddress; // Destination address of the shipment

    @SerializedName("origin_address")
    private Address originAddress; // Origin address of the shipment

    /**
     * ID of origin address. Required if the origin_address object is not provided
     */
    @SerializedName("origin_address_id")
    private Address originAddressId;

    /**
     * Sender Address. Only applies if the courier allows a different address to be displayed on
     * the label. If not specified, the origin address is used by default.
     */
    @SerializedName("sender_address")
    private Address senderAddress;

    /**
     * ID of sender address. Required if the sender_address object is not provided
     */
    @SerializedName("sender_address_id")
    private String senderAddressId;

    /**
     * Return Address. If not specified, the origin address is used by default.
     */
    @SerializedName("return_address")
    private Address returnAddress;

    private String incoterms; // Terms of sale (e.g., DDU)

    private Insurance insurance; // Insurance details for the shipment (paid option)

    /**
     * object
     * Set of key-value pairs that you can attach to a shipment. This can be useful for storing
     * additional information about the object in a structured format
     */
    private Metadata metadata;

    @SerializedName("order_data")
    private OrderData orderData; // Data related to the eCommerce platform

    @SerializedName("regulatory_identifiers")
    private RegulatoryIdentifiers regulatoryIdentifiers; // Seller's regulatory identifiers

    @SerializedName("return")
    private boolean isReturn; // Indicates if the shipment is a return

    @SerializedName("set_as_residential")
    private boolean setAsResidential; // Set the shipment as residential

    private List<RequestParcel> parcels; // List of parcels in the shipment

    @SerializedName("shipping_settings")
    private ShippingSettings shippingSettings; // Shipping settings for the shipment

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Metadata {
        // Map<String, String> data1;
        // Map<String, String> data2;
    }

}

