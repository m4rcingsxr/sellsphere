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
public class ProductPageRequest {

    private String[] filter;
    private String categoryAlias;
    private String keyword;
    private String sortBy;
    private Integer pageNum;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Integer categoryId;

    public ProductPageRequest(String[] filter, String categoryAlias, String keyword,
                              BigDecimal minPrice, BigDecimal maxPrice, Integer pageNum) {
        this.filter = filter;
        this.categoryAlias = categoryAlias;
        this.keyword = keyword;
        this.pageNum = pageNum;
    }
}
