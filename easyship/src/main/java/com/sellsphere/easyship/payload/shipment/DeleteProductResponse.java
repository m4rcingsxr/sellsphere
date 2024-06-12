package com.sellsphere.easyship.payload.shipment;

import com.sellsphere.easyship.payload.Meta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteProductResponse {

    public Meta meta;

    public Success success;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Success {
        public String message;
    }

}
