package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Represents shipping settings for a shipment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingSettings {

    @SerializedName("additional_services")
    private AdditionalServices additionalServices; // Additional services configuration

    private Units units; // Units for dimensions and weight

    /**
     * Create a shipment and buy the label in a single API call instead of two. Default: false.
     */
    @SerializedName("buy_label")
    private boolean buyLabel;

    /**
     * Generate a label synchronously. Returns a label in the response. Default: false.
     */
    @SerializedName("buy_label_synchronous")
    private boolean buyLabelSynchronous;

    @SerializedName("printing_options")
    private PrintingOptions printingOptions; // Format and page sizes of the shipping documents

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdditionalServices {

        /**
         * Generate QR code when generating label. If a courier does not support it, label will
         * be generated without the QR code. Currently officially supported only for USPS courier
         */
        @SerializedName("qr_code")
        private String qrCode;

        @SerializedName("delivery_confirmation")
        private String deliveryConfirmation; // Delivery confirmation type
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Units {

        private String weight; // Unit of weight (e.g., kg, g, lb, oz)

        private String dimensions; // Unit of dimensions (e.g., cm, in)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintingOptions {

        private String format; // Format of the shipping documents (e.g., png, pdf, url, zpl)

        private String label; // Label page size (e.g., 4x6, A4, A5)

        @SerializedName("commercial_invoice")
        private String commercialInvoice; // Commercial invoice page size

        @SerializedName("packing_slip")
        private String packingSlip; // Packing slip page size. Options: 4x6 / A4 (Default: 4x6)

    }
}