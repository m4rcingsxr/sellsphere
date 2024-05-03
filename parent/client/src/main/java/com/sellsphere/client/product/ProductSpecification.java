package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> filterProducts(ProductPageRequest pageRequest) {
        return Specification.where(ProductSpecifications.hasCategoryOrKeyword(pageRequest))
                .and(ProductSpecifications.hasDiscountPriceInRange(pageRequest))
                .and(ProductSpecifications.hasFilters(pageRequest));
    }

    public static Specification<Product> hasCategoryOrKeywordAndInPriceRange(ProductPageRequest pageRequest) {
        return Specification.where(ProductSpecifications.hasCategoryOrKeyword(pageRequest)
                                           .and(ProductSpecifications.hasDiscountPriceInRange(pageRequest)));
    }

    public static Specification<Product> hasCategoryOrKeyword(ProductPageRequest pageRequest) {
        return Specification.where(ProductSpecifications.hasCategoryOrKeyword(pageRequest));
    }

    // entity manager - limit 1
    public static Specification<Product> minPriceProduct(ProductPageRequest pageRequest) {
        return Specification.where(filterProducts(pageRequest)).and(ProductSpecifications.minPriceProduct());
    }

    public static Specification<Product> maxPriceProduct(ProductPageRequest pageRequest) {
        return Specification.where(filterProducts(pageRequest)).and(ProductSpecifications.maxPriceProduct());
    }

}

