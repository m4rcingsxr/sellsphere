package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Represents a line item in a shipment manifest; may be multiple physical objects.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelProductItemCreate {

    @SerializedName("declared_currency")
    private String declaredCurrency; // Item currency (ISO-4217 three-letter alphabetic currency code)

    @SerializedName("declared_customs_value")
    private double declaredCustomsValue; // Item customs value, must be greater than 0 unless category is documents

    private Product product; // Product details

    private int quantity; // Item quantity (defaults to 1)

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Product {
        private String id; // Product ID. Required if the sku is not provided
        private String sku; // Product SKU. Required if the id is not provided
    }
}