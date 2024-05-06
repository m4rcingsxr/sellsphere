package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ProductSpecifications {

    public static Specification<Product> hasCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> categoryJoin = root.join("category");
            return criteriaBuilder.or(
                    criteriaBuilder.equal(categoryJoin.get("id"), categoryId),
                    criteriaBuilder.like(categoryJoin.get("allParentIDs"), "%" + categoryId + "%")
            );
        };
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"),
                                                                      "%" + keyword + "%"
        );

    }

    public static Specification<Product> hasFilters(String[] filterStrings) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterStrings != null && filterStrings.length > 0) {
                List<Filter> filters = ProductFilterParser.parseProductFilters(filterStrings);

                for (Filter filter : filters) {

                    if ("Brand".equals(filter.getName())) {
                        predicates.add(criteriaBuilder.equal(root.get("brand").get("name"),
                                                             filter.getValue()
                        ));
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

    public static Specification<Product> hasDiscountPriceInRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            Expression<BigDecimal> discountedPrice = getDiscountPriceExpression(root, criteriaBuilder);

            return criteriaBuilder.between(discountedPrice, minPrice, maxPrice);
        };
    }


    public static Specification<Product> hasMinOrMaxDiscountPrice(boolean isMinPrice) {
        return (root, query, criteriaBuilder) -> {
            Expression<BigDecimal> discountedPrice = getDiscountPriceExpression(root, criteriaBuilder);
            Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
            Root<Product> subRoot = subquery.from(Product.class);
            Expression<BigDecimal> subDiscountedPrice = getDiscountPriceExpression(subRoot, criteriaBuilder);
            subquery.select(isMinPrice ? criteriaBuilder.min(subDiscountedPrice) : criteriaBuilder.max(subDiscountedPrice));
            return criteriaBuilder.equal(discountedPrice, subquery);
        };
    }

    private static Expression<BigDecimal> getDiscountPriceExpression(Root<Product> root, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.diff(
                root.get("price"),
                criteriaBuilder.prod(
                        root.get("price"),
                        criteriaBuilder.quot(root.get("discountPercent"), BigDecimal.valueOf(100))
                ).as(BigDecimal.class)
        ).as(BigDecimal.class);
    }

}