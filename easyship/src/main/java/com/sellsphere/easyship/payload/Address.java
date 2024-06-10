package com.sellsphere.easyship.payload;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    private String id;

    private String city;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("contact_email")
    private String contactEmail;

    @SerializedName("contact_name")
    private String contactName;

    @SerializedName("contact_phone")
    private String contactPhone;

    @SerializedName("country_alpha2")
    private String countryAlpha2;

    @SerializedName("default_for")
    private DefaultFor defaultFor;

    @SerializedName("line_1")
    private String line1;

    @SerializedName("line_2")
    private String line2;

    @SerializedName("postal_code")
    private String postalCode;

    private String state;

    private String currencyCode;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DefaultFor {
        private boolean pickup;
        private boolean billing;
        private boolean sender;

        @SerializedName("return_address")
        private boolean returnAddress;

    }

}