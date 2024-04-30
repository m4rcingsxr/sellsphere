package com.sellsphere.client.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageRequest {

    private String[] filter;
    private String categoryAlias;
    private String keyword;
    private Integer pageNum;

}
