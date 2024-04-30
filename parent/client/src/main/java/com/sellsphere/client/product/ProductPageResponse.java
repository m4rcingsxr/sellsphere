package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductPageResponse {

    private int page;
    private int pageSize;
    private int totalElements;
    private int totalPages;
    private List<Product> content;

}
