package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import com.sellsphere.easyship.payload.Meta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    @SerializedName("meta")
    private Meta meta;

    @SerializedName("product")
    private Product product;

}
