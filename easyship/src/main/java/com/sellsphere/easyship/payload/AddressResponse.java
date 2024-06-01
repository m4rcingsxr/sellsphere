package com.sellsphere.easyship.payload;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {

    private List<AddressDto> addresses;
    private Meta meta;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Meta {
        private Pagination pagination;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public static class Pagination {
            private int page;
            private String next;
            private int count;
        }
    }
}