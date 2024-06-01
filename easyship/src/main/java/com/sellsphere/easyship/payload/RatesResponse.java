package com.sellsphere.easyship.payload;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RatesResponse {
    private Meta meta;
    private List<Rate> rates;
}