package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.MoneyUtil;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetailDto;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicProductDTO implements Serializable {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int DISCOUNT_SCALE = 4;
    private static final int PRICE_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

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

    boolean isOnTheWishlist;

    float averageRating;



    List<ProductDetailDto> details;

    public BasicProductDTO(Product other, Customer customer) {
        this(other);
        if (customer != null) this.isOnTheWishlist = other.isOnTheWishlist(customer);
    }

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
        this.averageRating = other.getAverageRating();
    }

    @Transient
    public BigDecimal getDiscountPrice() {
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountMultiplier = ONE_HUNDRED.subtract(discountPercent);
            BigDecimal discountFactor = discountMultiplier.divide(ONE_HUNDRED, DISCOUNT_SCALE, ROUNDING_MODE);
            return price.multiply(discountFactor).setScale(PRICE_SCALE, ROUNDING_MODE);
        }
        return price.setScale(PRICE_SCALE, ROUNDING_MODE);
    }

    @Transient
    public String getShortName() {
        if (this.name.length() > 60) {
            return this.name.substring(0, 60).concat("...");
        }
        return this.name;
    }

    public void setWishlistAssigned(Customer customer) {

    }
}