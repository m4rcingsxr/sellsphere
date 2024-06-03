package com.sellsphere.easyship.payload;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDtoMin {

    private String fullName;

    private String city;

    @SerializedName("country_alpha2")
    private String countryAlpha2;

    private String currencyCode;

    @SerializedName("postal_code")
    private String postalCode;

    @SerializedName("line_1")
    private String line1;

    @SerializedName("line_2")
    private String line2;

    @SerializedName("state")
    private String state;

}