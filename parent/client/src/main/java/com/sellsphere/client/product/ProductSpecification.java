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
                List<Filter> filters = parseProductFilters(filterStrings);
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
    private static List<Filter> parseProductFilters(String[] filters) {
        // Validate filters length
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("Filters are required in format: [filter...] ; filter='name,value'");
        }

        // Parse filters
        boolean isSingle = Arrays.stream(filters).noneMatch(filter -> filter.contains(","));

        if (isSingle) {
            return List.of(parseFilter(String.join(",", filters)));
        } else {
            return Arrays.stream(filters)
                    .map(ProductSpecification::parseFilter)
                    .toList();
        }
    }

    private static Filter parseFilter(String csvFilter) {

        // allow filter values to have commas when they are surrounded with single quote
        String[] filters = CSVParser.parseCSV(csvFilter);

        // Validate if each entry contains at least 2 values
        if (filters.length != 2) {
            throw new IllegalArgumentException(csvFilter + " is incorrect. Each filter must have two values: filter='name,value'");
        }

        String name = filters[0].trim();
        String value = filters[1].trim();

        if (name.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Filter name and value cannot be empty.");
        }

        return new Filter(name, value);
    }
}
