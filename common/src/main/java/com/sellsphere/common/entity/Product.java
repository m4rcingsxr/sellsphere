package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a product entity in the system. This class is mapped to the "products" table
 * in the database and includes various attributes that define a product, such as name,
 * description, price, dimensions, and related entities like brand and category.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends IdentifiedEntity {

    /**
     * The name of the product.
     * This is a unique identifier that represents the product's name.
     */
    @NotNull(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Column(name = "name", unique = true, length = 255, nullable = false)
    private String name;

    /**
     * The alias of the product.
     * This is an alternate name for the product, often used in URLs or as a search key.
     */
    @Size(max = 255, message = "Alias must be up to 255 characters")
    @Column(name = "alias", unique = true, length = 255, nullable = false)
    private String alias;

    /**
     * A short description of the product.
     * This is a brief overview of the product, typically used in listings and summaries.
     */
    @NotNull(message = "Short description is required")
    @Size(min = 1, max = 2048, message = "Short description must be between 1 and 2048 characters")
    @Column(name = "short_description", length = 2048, nullable = false)
    private String shortDescription;

    /**
     * A detailed description of the product.
     * This is a comprehensive description providing all necessary information about the product.
     */
    @Lob
    @NotNull(message = "Long description is required")
    @Size(min = 1, max = 4096, message = "Long description must be between 1 and 4096 characters")
    @Column(name = "full_description", columnDefinition = "TEXT", nullable = false)
    private String fullDescription;

    /**
     * The creation time of the product.
     * This timestamp indicates when the product was created in the system.
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * The product update information.
     * This links to a record of when the product was last updated.
     */
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductUpdate productUpdate;

    /**
     * Indicates whether the product is enabled.
     * This boolean flag determines if the product is active and should be displayed to users.
     */
    @Column(name = "enabled", nullable = false, columnDefinition = "TINYINT")
    private boolean enabled = true;

    /**
     * Indicates whether the product is in stock.
     * This boolean flag shows if the product is available for purchase.
     */
    @Column(name = "in_stock", nullable = false)
    private boolean inStock = true;

    /**
     * The cost price of the product.
     * This is the amount it costs to acquire or produce the product.
     */
    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Cost must be a valid amount with up to 2 " +
            "decimal places")
    @Column(name = "cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    /**
     * The selling price of the product.
     * This is the price at which the product is sold to customers.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid amount with up to 2 " +
            "decimal places")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * The discount percentage applied to the product.
     * This represents the discount offered on the product's price.
     */
    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.0", message = "Discount percent must be greater than or equal to zero")
    @DecimalMax(value = "100.00", message = "Discount percent must be less than or equal to 100.00")
    @Digits(integer = 4, fraction = 2, message = "Discount percent must be a valid percentage " +
            "with up to 2 decimal places")
    @Column(name = "discount_percent", nullable = false, precision = 4, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    /**
     * The length of the product.
     * This dimension is used for shipping and storage calculations.
     */
    @NotNull(message = "Length is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Length must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Length must be a valid amount with up to 2 " +
            "decimal places")
    @Column(name = "length", nullable = false, precision = 12, scale = 2)
    private BigDecimal length;

    /**
     * The width of the product.
     * This dimension is used for shipping and storage calculations.
     */
    @NotNull(message = "Width is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Width must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Width must be a valid amount with up to 2 " +
            "decimal places")
    @Column(name = "width", nullable = false, precision = 12, scale = 2)
    private BigDecimal width;

    /**
     * The height of the product.
     * This dimension is used for shipping and storage calculations.
     */
    @NotNull(message = "Height is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Height must be a valid amount with up to 2 " +
            "decimal places")
    @Column(name = "height", nullable = false, precision = 12, scale = 2)
    private BigDecimal height;

    /**
     * The weight of the product.
     * This attribute is crucial for shipping cost calculations.
     */
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Weight must be a valid amount with up to 2 " +
            "decimal places")
    @Column(name = "weight", nullable = false, precision = 12, scale = 2)
    private BigDecimal weight;

    /**
     * The filename of the main image for the product.
     * This image is displayed as the primary visual representation of the product.
     */
    @Column(name = "main_image")
    private String mainImage;

    /**
     * The category to which the product belongs.
     * This is a reference to the category entity.
     */
    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * The brand of the product.
     * This is a reference to the brand entity.
     */
    @NotNull(message = "Brand is required")
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /**
     * A list of additional images for the product.
     * These images provide more visual details about the product.
     */
    @OrderBy("id asc")
    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    /**
     * A list of details about the product.
     * These details provide specific information about the product's features.
     */
    @OrderBy("name asc")
    @OneToMany(mappedBy = "product", cascade = {
            CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductDetail> details = new ArrayList<>();

    @NotNull(message = "Product tax must be provided")
    @ManyToOne
    @JoinColumn(name = "tax_id")
    private ProductTax tax;

    /**
     * This boolean field indicates whether the item contains lithium-ion batteries that are
     * packed with equipment. These are rechargeable batteries commonly used in consumer
     * electronics, such as smartphones, laptops, and other portable devices.
     * Packing Instruction 966 must be applied : Applies when lithium-ion batteries are packed
     * separately from the equipment but shipped together in the same package
     */
    @Column(name = "contains_baterry_pi966", nullable = false)
    private boolean containsBatteryPi966;

    /**
     * Packing Instruction 967: Applies when lithium-ion batteries are installed in the equipment
     * being shipped.
     * ContainsBatteryPi967: This boolean field indicates whether the item contains lithium-ion
     * batteries that are installed in equipment.
     * Lithium-Ion Batteries: As above, these are rechargeable batteries used in various
     * electronic devices.
     */
    @Column(name = "contains_baterry_pi967", nullable = false)
    private boolean containsBatteryPi967;

    @Column(name = "contains_liquids", nullable = false)
    private boolean containsLiquids;

    /**
     * Harmonized Commodity Description and Coding System. An HS code consists of at least six
     * digits and is used by customs to classify the product being shipped. That way it can
     * accurately calculate taxes and duties and apply any necessary restrictions.
     */
    @Column(name = "hs_code", nullable = false)
    private String hsCode;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "average_rating")
    private Float averageRating;

    public Product(Integer id) {
        this.id = id;
    }

    /**
     * Gets the path to the main image of the product.
     * If no image is available, returns the path to a default image.
     *
     * @return the main image path.
     */
    @Transient
    public String getMainImagePath() {
        return Constants.S3_BASE_URI + (id == null || mainImage == null ? "/default.png" :
                "/product-photos/" + this.id + "/" + mainImage);
    }

    @Transient
    public BigDecimal getDiscountPrice() {
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountMultiplier = BigDecimal.valueOf(100).subtract(
                    discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return price.multiply(discountMultiplier);
        }

        return this.price;
    }


    /**
     * Adds a detail to the product.
     *
     * @param productDetail the detail to add.
     */
    public void addProductDetail(ProductDetail productDetail) {
        productDetail.setProduct(this);
        details.add(productDetail);
    }

    /**
     * Adds an image to the product.
     *
     * @param productImage the image to add.
     */
    public void addProductImage(ProductImage productImage) {
        productImage.setProduct(this);
        images.add(productImage);
    }

    /**
     * Updates the product's timestamp.
     * This method sets the current time as the product's last update time.
     */
    public void updateProductTimestamp() {
        if (this.productUpdate == null) {
            ProductUpdate newProductUpdate = new ProductUpdate();
            newProductUpdate.setProduct(this);
            newProductUpdate.setUpdatedTime(LocalDateTime.now());

            this.productUpdate = newProductUpdate;
        } else {
            this.productUpdate.setUpdatedTime(LocalDateTime.now());
        }
    }

    public Float getAverageRating() {
        if (this.averageRating == null) return 0.0F;

        return this.averageRating;
    }

    public Integer getReviewCount() {
        if (this.averageRating == null) return 0;

        return this.reviewCount;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
                o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
                this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Product product = (Product) o;
        return getId() != null && Objects.equals(getId(), product.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
