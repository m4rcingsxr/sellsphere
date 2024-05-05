package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> filterProductsByCategoryInPriceBoundaries(
            Integer categoryId,
            String[] filters,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        return Specification.where(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.hasDiscountPriceInRange(minPrice, maxPrice))
                .and(ProductSpecifications.hasFilters(filters));
    }

    public static Specification<Product> filterProductsByKeywordInPriceBoundaries(String keyword,
                                                                                  String[] filters,
                                                                                  BigDecimal minPrice,
                                                                                  BigDecimal maxPrice) {
        return Specification.where(ProductSpecifications.hasKeyword(keyword))
                .and(ProductSpecifications.hasDiscountPriceInRange(minPrice, maxPrice))
                .and(ProductSpecifications.hasFilters(filters));
    }

    public static Specification<Product> filterProductsByCategory(Integer categoryId,
                                                                  String[] filters) {
        return Specification.where(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.hasFilters(filters));
    }

    public static Specification<Product> filterProductsByKeyword(String keyword, String[] filters) {
        return Specification.where(ProductSpecifications.hasKeyword(keyword))
                .and(ProductSpecifications.hasFilters(filters));
    }

    public static Specification<Product> productsByCategoryInPriceBoundaries(Integer categoryId,
                                                                  BigDecimal minPrice, BigDecimal maxPrice) {
        return Specification.where(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.hasDiscountPriceInRange(minPrice, maxPrice));
    }

    public static Specification<Product> productsByKeywordInPriceBoundaries(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        return Specification.where(ProductSpecifications.hasKeyword(keyword))
                .and(ProductSpecifications.hasDiscountPriceInRange(minPrice, maxPrice));
    }


    public static Specification<Product> minDiscountPriceInFilteredProductsByCategory(
            Integer categoryId,
            String[] filters,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        return Specification
                .where(filterProductsByCategoryInPriceBoundaries(categoryId, filters, minPrice,
                                                                 maxPrice
                ))
                .and(ProductSpecifications.minPriceProduct());
    }

    public static Specification<Product> maxDiscountPriceInFilteredProductsByCategory(
            Integer categoryId,
            String[] filters,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        return Specification
                .where(filterProductsByCategoryInPriceBoundaries(categoryId, filters, minPrice,
                                                                 maxPrice
                ))
                .and(ProductSpecifications.maxPriceProduct());
    }

    public static Specification<Product> minDiscountPriceInFilteredProductsByKeyword(String keyword,
                                                                                     String[] filters,
                                                                                     BigDecimal minPrice,
                                                                                     BigDecimal maxPrice) {
        return Specification
                .where(filterProductsByKeywordInPriceBoundaries(keyword, filters, minPrice,
                                                                maxPrice
                ))
                .and(ProductSpecifications.minPriceProduct());
    }

    public static Specification<Product> maxDiscountPriceInFilteredProductsByKeyword(String keyword,
                                                                                     String[] filters,
                                                                                     BigDecimal minPrice,
                                                                                     BigDecimal maxPrice) {
        return Specification
                .where(filterProductsByKeywordInPriceBoundaries(keyword, filters, minPrice,
                                                                maxPrice
                ))
                .and(ProductSpecifications.maxPriceProduct());
    }


}

