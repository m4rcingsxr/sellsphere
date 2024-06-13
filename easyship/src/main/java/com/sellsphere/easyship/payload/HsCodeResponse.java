package com.sellsphere.easyship.payload;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsCodeResponse {

    private Meta meta;

    @SerializedName("hs_codes")
    List<HsCode> hsCodes;
}
