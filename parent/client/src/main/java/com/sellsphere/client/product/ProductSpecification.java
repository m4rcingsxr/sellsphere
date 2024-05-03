package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> filterProducts(ProductPageRequest pageRequest) {
        return Specification.where(ProductSpecifications.hasCategoryOrKeyword(pageRequest))
                .and(ProductSpecifications.hasPriceInRange(pageRequest))
                .and(ProductSpecifications.hasFilters(pageRequest));
    }

    public static Specification<Product> hasCategoryOrKeywordAndInPriceRange(ProductPageRequest pageRequest) {
        return Specification.where(ProductSpecifications.hasCategoryOrKeyword(pageRequest)
                                           .and(ProductSpecifications.hasPriceInRange(pageRequest)));
    }

    public static Specification<Product> hasCategoryOrKeyword(ProductPageRequest pageRequest) {
        return Specification.where(ProductSpecifications.hasCategoryOrKeyword(pageRequest));
    }

}

