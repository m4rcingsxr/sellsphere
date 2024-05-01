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
            List<Predicate> filterPredicates = new ArrayList<>();

            // Handle category or keyword
            if (pageRequest.getCategoryAlias() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("alias"), pageRequest.getCategoryAlias()));
            } else if (pageRequest.getKeyword() != null) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + pageRequest.getKeyword() + "%"));
            } else {
                throw new IllegalArgumentException("Either category alias or keyword must be provided");
            }

            // Handle filters
            String[] filterStrings = pageRequest.getFilter();
            if (filterStrings != null && filterStrings.length > 0) {
                List<Filter> filters = parseProductFilters(filterStrings);
                for (Filter filter : filters) {
                    if ("brand".equals(filter.getName())) {
                        filterPredicates.add(criteriaBuilder.equal(root.get("brand").get("name"), filter.getValue()));
                    } else {
                        Join<Product, ProductDetail> detailsJoin = root.join("details", JoinType.INNER);
                        filterPredicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(detailsJoin.get("name"), filter.getName()),
                                criteriaBuilder.equal(detailsJoin.get("value"), filter.getValue())
                        ));
                    }
                }
                if (!filterPredicates.isEmpty()) {
                    predicates.addAll(filterPredicates);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    private static List<Filter> parseProductFilters(String[] filters) {
        // 1) 1 value always indicate the name of filter
        // 2) 1 filter query param - filters = array of this one entry
        // 3) >= 1 filter query param - filters = Array of csv that indicate filter value

        // validate filters length
        if(filters == null || filters.length == 0) {
            throw new IllegalArgumentException("Filters are required in format : [filter...] ; filter='name,value'");
        }

        // parse filters
        // check if it single filter
        boolean isSingle = Arrays.stream(filters).noneMatch(filter -> filter.contains(","));

        if(isSingle) {
            return List.of(parseFilter(String.join(",", filters)));
        } else {
            return Arrays.stream(filters)
                    .map(ProductSpecification::parseFilter)
                    .toList();
        }
    }

    private static Filter parseFilter(String csvFilter) {
        String[] filters = csvFilter.split(",");

        // validate if each entry if it does contains csv have min 2 values..
        if(filters.length < 2) {
            throw new IllegalArgumentException(csvFilter + " is incorrect. Each filter must have two values : filter='name,value'");
        }

        String name = filters[0].trim();
        String value = filters[1].trim();

        if(name.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Filter name and value cannot be empty.");
        }

        return new Filter(name, value);
    }

}
