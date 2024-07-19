package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Promotion;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.sellsphere.common.entity.Promotion}
 */
@Value
public class PromotionDTO implements Serializable {

    Integer id;

    String name;

    List<BasicProductDTO> products;

    public PromotionDTO(Promotion promotion) {
        this.id = promotion.getId();
        this.name = promotion.getName();
        this.products = promotion.getProducts().stream().map(BasicProductDTO::new).toList();
    }

}