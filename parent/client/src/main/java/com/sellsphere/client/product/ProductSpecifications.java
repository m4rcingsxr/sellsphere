package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> hasCategoryOrKeyword(ProductPageRequest pageRequest) {
        return (root, query, criteriaBuilder) -> {
            if (pageRequest.getCategoryAlias() != null) {
                Join<Object, Object> categoryJoin = root.join("category");
                return criteriaBuilder.or(
                        criteriaBuilder.equal(categoryJoin.get("alias"), pageRequest.getCategoryAlias()),
                        criteriaBuilder.like(categoryJoin.get("allParentIDs"), "%" + pageRequest.getCategoryId() + "%")
                );
            } else if (pageRequest.getKeyword() != null) {
                return criteriaBuilder.like(root.get("name"), "%" + pageRequest.getKeyword() + "%");
            } else {
                throw new IllegalArgumentException("Either category alias or keyword must be provided");
            }
        };
    }

    public static Specification<Product> hasPriceInRange(ProductPageRequest pageRequest) {
        return (root, query, criteriaBuilder) -> {
            BigDecimal minPrice = pageRequest.getMinPrice();
            BigDecimal maxPrice = pageRequest.getMaxPrice();

            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else if (maxPrice != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            } else {
                return criteriaBuilder.conjunction(); // No price filter
            }
        };
    }

    public static Specification<Product> hasFilters(ProductPageRequest pageRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            String[] filterStrings = pageRequest.getFilter();
            if (filterStrings != null && filterStrings.length > 0) {
                List<Filter> filters = ProductFilterParser.parseProductFilters(filterStrings);
                for (Filter filter : filters) {
                    if ("brand".equals(filter.getName())) {
                        predicates.add(criteriaBuilder.equal(root.get("brand").get("name"), filter.getValue()));
                    } else {
                        Join<Product, ProductDetail> detailsJoin = root.join("details", JoinType.INNER);
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(detailsJoin.get("name"), filter.getName()),
                                criteriaBuilder.equal(detailsJoin.get("value"), filter.getValue())
                        ));
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}