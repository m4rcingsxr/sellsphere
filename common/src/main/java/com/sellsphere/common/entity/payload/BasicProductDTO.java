package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetailDto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasicProductDTO implements Serializable {

    Integer id;

    @NotNull(message = "Name is required")
    @Size(message = "Name must be between 1 and 255 characters", min = 1, max = 255)
    String name;

    @Size(message = "Alias must be up to 255 characters", max = 255)
    String alias;
    boolean inStock;

    @NotNull(message = "Price is required")
    @Digits(message = "Price must be a valid amount with up to 2 decimal places", integer = 10,
            fraction = 2)
    BigDecimal price;

    @NotNull(message = "Discount percent is required")
    @Digits(message = "Discount percent must be a valid percentage with up to 2 decimal places",
            integer = 4, fraction = 2)
    BigDecimal discountPercent;

    BigDecimal discountPrice;

    String categoryName;

    String brandName;

    String mainImagePath;


    List<ProductDetailDto> details;

    public BasicProductDTO(Product other) {
        this.id = other.getId();
        this.name = other.getName();
        this.discountPrice = other.getDiscountPrice();
        this.alias = other.getAlias();
        this.inStock = other.isInStock();
        this.price = other.getPrice();
        this.discountPercent = other.getDiscountPercent();
        this.categoryName = other.getCategory().getName();
        this.brandName = other.getBrand().getName();
        this.mainImagePath = other.getMainImagePath();
        this.details = other.getDetails().stream().map(ProductDetailDto::new).toList();
    }
}