package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Brand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.sellsphere.common.entity.Brand}
 */
@Value
public class BrandDTO implements Serializable {
    Integer id;
    @NotNull(message = "Brand name is required")
    @Size(message = "brand name must be between 1 and 45 characters", min = 1, max = 45)
    String name;

    public BrandDTO(Brand brand) {
        this.id = brand.getId();
        this.name = brand.getName();
    }
}