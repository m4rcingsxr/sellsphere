package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@UtilityClass
public class ProductSpecification {

    public static Specification<Product> filterProducts(
            Integer categoryId,
            String keyword,
            String[] filters,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        Specification<Product> spec = hasCategoryAndKeyword(categoryId, keyword);

        if (filters != null && filters.length > 0) {
            spec = spec.and(ProductSpecifications.hasFilters(filters));
        }
        if (minPrice != null && maxPrice != null) {
            spec = spec.and(ProductSpecifications.hasDiscountPriceInRange(minPrice, maxPrice));
        }

        return spec;
    }

    public static Specification<Product> hasCategoryAndKeyword(Integer categoryId, String keyword) {
        Specification<Product> spec = Specification.where(null);

        if (categoryId != null) {
            spec = spec.and(ProductSpecifications.hasCategory(categoryId));
        }

        if (keyword != null) {
            spec = spec.and(ProductSpecifications.hasKeyword(keyword));
        }
        return spec;
    }

    public static Specification<Product> minDiscountPrice(Integer categoryId, String keyword,
                                                          Specification<Product> baseSpec) {
        return baseSpec.and(
                ProductSpecifications.hasMinOrMaxDiscountPrice(categoryId, keyword, true));
    }

    public static Specification<Product> maxDiscountPrice(Integer categoryId, String keyword,
                                                          Specification<Product> baseSpec) {
        return baseSpec.and(
                ProductSpecifications.hasMinOrMaxDiscountPrice(categoryId, keyword, false));
    }


}

