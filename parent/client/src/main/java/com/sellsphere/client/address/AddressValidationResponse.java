package com.sellsphere.client.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressValidationResponse {
    private Result result;
    private String responseId;
    private List<String> invalidFields;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private Verdict verdict;
        private Address address;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Verdict {
            private String inputGranularity;
            private String validationGranularity;
            private boolean addressComplete;
            private boolean hasUnconfirmedComponents;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Address {
            private List<AddressComponent> addressComponents;

            @Getter
            @Setter
            @AllArgsConstructor
            @NoArgsConstructor
            public static class AddressComponent {
                private String componentType;
                private String confirmationLevel;
            }
        }
    }
}