package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Represents buyer's identifiers for various tax, import, and export regulations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerRegulatoryIdentifiers {

    private String ein; // Employer Identification Number (EIN)

    @SerializedName("vat_number")
    private String vatNumber; // Value-Added Tax (VAT) number
}