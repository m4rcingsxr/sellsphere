package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @SerializedName("comments")
    private String comments; // Additional comments about the product.

    @SerializedName("contains_battery_pi966")
    private Boolean containsBatteryPi966; // Check if the product should apply packing instruction 966.

    @SerializedName("contains_battery_pi967")
    private Boolean containsBatteryPi967; // Check if the product should apply packing instruction 967.

    @SerializedName("contains_liquids")
    private Boolean containsLiquids; // Check if the product contains liquid.

    @SerializedName("cost_price")
    private Double costPrice; // Cost of the product.

    @SerializedName("cost_price_currency")
    private String costPriceCurrency; // Product cost currency. ISO-4217 three-letter alphabetic currency code (e.g. USD, EUR, GBP).

    @SerializedName("created_at")
    private String createdAt; // Timestamp of when the product was created.

    @SerializedName("height")
    private Double height; // Height of the product in cm (centimeters).

    @SerializedName("hs_code")
    private String hsCode; // Harmonized System Code for the product.

    @SerializedName("id")
    private String id; // Product ID.

    @SerializedName("identifier")
    private String identifier; // SKU for the product. This should be unique per one store. Identifier is required when store_id or platform_product_id is empty.

    @SerializedName("image_url")
    private String imageUrl; // Image URL of the product.

    @SerializedName("input_type")
    private String inputType; // Type of input for the product.

    @SerializedName("length")
    private Double length; // Length of the product in cm (centimeters).

    @SerializedName("name")
    private String name; // Human-readable name of the product.

    @SerializedName("origin_country_alpha2")
    private String originCountryAlpha2; // Country where the product is manufactured. Country Code in Alpha-2 format (ISO 3166-1).

    @SerializedName("pick_location")
    private String pickLocation; // Pickup location for the product.

    @SerializedName("platform_product_id")
    private String platformProductId; // Platform-specific product ID.

    @SerializedName("selling_price")
    private Double sellingPrice; // Product selling price.

    @SerializedName("selling_price_currency")
    private String sellingPriceCurrency; // Currency of the product selling price. ISO-4217 three-letter alphabetic currency code (e.g. USD, EUR, GBP).

    @SerializedName("store_id")
    private String storeId; // Store ID.

    @SerializedName("updated_at")
    private String updatedAt; // Timestamp of when the product was last updated.

    @SerializedName("weight")
    private Double weight; // Weight of the product in kg (kilograms).

    @SerializedName("width")
    private Double width; // Width of the product in cm (centimeters).
}
