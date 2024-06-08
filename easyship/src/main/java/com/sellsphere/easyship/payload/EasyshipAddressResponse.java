package com.sellsphere.easyship.payload;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EasyshipAddressResponse {

    private List<EasyshipAddress> addresses;
    private Meta meta;

}