package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(ProductPageRequest pageRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Handle category or keyword
            if (pageRequest.getCategoryAlias() != null) {
                Join<Object, Object> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(categoryJoin.get("alias"), pageRequest.getCategoryAlias()),
                        criteriaBuilder.like(categoryJoin.get("allParentIDs"), "%" + pageRequest.getCategoryId() + "%")
                ));
            } else if (pageRequest.getKeyword() != null) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + pageRequest.getKeyword() + "%"));
            } else {
                throw new IllegalArgumentException("Either category alias or keyword must be provided");
            }

            // Handle filters
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
