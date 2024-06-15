package com.sellsphere.easyship.payload;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.beans.Transient;

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

    @Transient
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();

        if (companyName != null && !companyName.isEmpty()) {
            fullAddress.append(companyName).append("\n");
        }

        if (contactName != null && !contactName.isEmpty()) {
            fullAddress.append(contactName).append("\n");
        }

        if (line1 != null && !line1.isEmpty()) {
            fullAddress.append(line1).append("\n");
        }

        if (line2 != null && !line2.isEmpty()) {
            fullAddress.append(line2).append("\n");
        }

        if (city != null && !city.isEmpty()) {
            fullAddress.append(city).append(", ");
        }

        if (state != null && !state.isEmpty()) {
            fullAddress.append(state).append(" ");
        }

        if (postalCode != null && !postalCode.isEmpty()) {
            fullAddress.append(postalCode).append("\n");
        }

        if (countryAlpha2 != null && !countryAlpha2.isEmpty()) {
            fullAddress.append(countryAlpha2).append("\n");
        }

        if (contactPhone != null && !contactPhone.isEmpty()) {
            fullAddress.append("Phone: ").append(contactPhone).append("\n");
        }

        if (contactEmail != null && !contactEmail.isEmpty()) {
            fullAddress.append("Email: ").append(contactEmail).append("\n");
        }

        return fullAddress.toString().trim();
    }
}