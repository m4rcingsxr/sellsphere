package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

/**
 * Represents free-form data related to the eCommerce platform.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderData {

    /**
     * A display-ready sales channel or platform name
     */
    @SerializedName("platform_name")
    private String platformName;

    /**
     * Order number that was either copied from an order synced from an ecommerce platform or
     * manually edited in Easyship's order from
     */
    @SerializedName("platform_order_number")
    private String platformOrderNumber;

    /**
     * Text added by the merchant. Will not be shown to the receiver
     */
    @SerializedName("seller_notes")
    private String sellerNotes;

    /**
     * Text added by the buyer during order checkout. Will be displayed on the packing slip.
     */
    @SerializedName("buyer_notes")
    private String buyerNotes;

    /**
     * Courier name for shipping rule condition match_buyer_selected_courier_name. If the name
     * matches, actions of this shipping rule would apply to the current shipment. rules: paid
     * option
     */
    @SerializedName("buyer_selected_courier_name")
    private String buyerSelectedCourierName;

    /**
     * order_created_at
     * When the order was created (e.g. in an online store connected to Easyship)
     */
    @SerializedName("order_created_at")
    private String orderCreatedAt;

    /**
     * Tags that have been assigned to this shipment, either as an order on its e-commerce store
     * or within the Easyship app
     */
    @SerializedName("order_tag_list")
    private List<String> orderTagList;
}