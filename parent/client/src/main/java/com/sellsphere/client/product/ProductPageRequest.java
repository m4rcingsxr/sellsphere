package com.sellsphere.client.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductPageRequest {

    private String[] filter;
    private String categoryAlias;
    private String keyword;
    private Integer pageNum;

    private Integer categoryId;

    public ProductPageRequest(String[] filter, String categoryAlias, String keyword,
                              Integer pageNum) {
        this.filter = filter;
        this.categoryAlias = categoryAlias;
        this.keyword = keyword;
        this.pageNum = pageNum;
    }
}
