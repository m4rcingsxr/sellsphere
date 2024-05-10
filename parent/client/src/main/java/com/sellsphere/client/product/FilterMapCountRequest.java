package com.sellsphere.client.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidFilterRequest
public class FilterMapCountRequest {

    private String[] filter;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String categoryAlias;

    private String keyword;

    private Integer categoryId;

}
