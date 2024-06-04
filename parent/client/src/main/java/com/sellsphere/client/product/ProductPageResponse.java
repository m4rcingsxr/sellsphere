package com.sellsphere.client.product;

import com.sellsphere.common.entity.payload.BasicProductDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductPageResponse {

    private int page;
    private long totalElements;
    private int totalPages;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private List<BasicProductDTO> content;

}
