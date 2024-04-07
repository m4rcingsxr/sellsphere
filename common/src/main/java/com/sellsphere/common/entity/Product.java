package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends IdentifiedEntity {

    @NotNull(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Column(name = "name", unique = true, length = 255, nullable = false)
    private String name;

    @NotBlank(message = "Alias is required")
    @Size(max = 255, message = "Alias must be up to 255 characters")
    @Column(name = "alias", unique = true, length = 255, nullable = false)
    private String alias;

    @NotNull(message = "Short description is required")
    @Size(min = 1, max = 2048, message = "Short description must be between 1 and 2048 characters")
    @Column(name = "short_description", length = 2048, nullable = false)
    private String shortDescription;

    @NotNull(message = "Long description is required")
    @Size(min = 1, max = 4096, message = "Long description must be between 1 and 4096 characters")
    @Column(name = "full_description", columnDefinition = "CLOB", nullable = false)
    private String fullDescription;

    @NotNull(message = "Created time is required")
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductUpdate productUpdate;

    @Column(name = "enabled", nullable = false, columnDefinition = "TINYINT")
    private boolean enabled = true;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock = true;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Cost must be a valid amount with up to 2 decimal places")
    @Column(name = "cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid amount with up to 2 decimal places")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.0", message = "Discount percent must be greater than or equal to zero")
    @DecimalMax(value = "100.00", message = "Discount percent must be less than or equal to 100.00")
    @Digits(integer = 4, fraction = 2, message = "Discount percent must be a valid percentage with up to 2 decimal places")
    @Column(name = "discount_percent", nullable = false, precision = 4, scale = 2)
    private BigDecimal discountPercent;

    @NotNull(message = "Length is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Length must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Length must be a valid amount with up to 2 decimal places")
    @Column(name = "length", nullable = false, precision = 12, scale = 2)
    private BigDecimal length;

    @NotNull(message = "Width is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Width must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Width must be a valid amount with up to 2 decimal places")
    @Column(name = "width", nullable = false, precision = 12, scale = 2)
    private BigDecimal width;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Height must be a valid amount with up to 2 decimal places")
    @Column(name = "height", nullable = false, precision = 12, scale = 2)
    private BigDecimal height;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Weight must be a valid amount with up to 2 decimal places")
    @Column(name = "weight", nullable = false, precision = 12, scale = 2)
    private BigDecimal weight;

    @NotBlank(message = "Main image is required")
    @Column(name = "main_image", nullable = false)
    private String mainImage;

    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull(message = "Brand is required")
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OrderBy("name asc")
    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ProductDetail> details = new ArrayList<>();

    @Transient
    public String getMainImagePath() {
        return Constants.S3_BASE_URI + (id == null || mainImage == null ? "/default.png" :
                "/product-photos/" + this.id + "/" + mainImage);
    }

    public void addProductDetail(ProductDetail productDetail) {
        productDetail.setProduct(this);
        details.add(productDetail);
    }

    public void addProductImage(ProductImage productImage) {
        productImage.setProduct(this);
        images.add(productImage);
    }

    public void updateProductTimestamp() {
        ProductUpdate newProductUpdate = new ProductUpdate();
        newProductUpdate.setProduct(this);
        newProductUpdate.setUpdatedTime(LocalDateTime.now());

        this.productUpdate = newProductUpdate;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Product product = (Product) o;
        return getId() != null && Objects.equals(getId(), product.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
