package com.sellsphere.common.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ProductDetail}
 */
@Value
public class ProductDetailDto implements Serializable {
    Integer id;
    @NotNull(message = "Name is required")
    @Size(message = "Name must be between 1 and 255 characters", min = 1, max = 255)
    String name;
    @NotNull(message = "Value is required")
    @Size(message = "Value must be between 1 and 255 characters", min = 1, max = 255)
    String value;

    public ProductDetailDto(ProductDetail productDetail) {
        this.id = productDetail.getId();
        this.name = productDetail.getName();
        this.value = productDetail.getValue();
    }
}