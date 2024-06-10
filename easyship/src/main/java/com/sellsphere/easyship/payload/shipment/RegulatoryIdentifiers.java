package com.sellsphere.easyship.payload.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Seller's identifiers for various tax, import and export regulations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegulatoryIdentifiers {

    private String eori; // Economic Operators Registration and Identification (EORI) number

    private String ioss; // Import One Stop Shop (IOSS) number

    @SerializedName("vat_number")
    private String vatNumber; // Value-Added Tax (VAT) number
}