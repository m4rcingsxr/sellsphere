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

            // Handle filters
            String[] filterStrings = pageRequest.getFilter();
            if (filterStrings != null) {
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

            // Handle category or keyword
            if (pageRequest.getCategoryAlias() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("alias"), pageRequest.getCategoryAlias()));
            } else if (pageRequest.getKeyword() != null) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + pageRequest.getKeyword() + "%"));
            } else {
                throw new IllegalArgumentException("Either category alias or keyword must be provided");
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
            throw new IllegalArgumentException("Filters are required in format : [filter...] ; filter='name,val1,val2'");
        }

        // parse filters
        // check if it single filter
        boolean isSingle = Arrays.stream(filters).noneMatch(filter -> filter.contains(","));

        if(isSingle) {
            return parseFilters(String.join(",", filters));
        } else {
            return Arrays.stream(filters)
                    .map(ProductSpecification::parseFilters)
                    .flatMap(List::stream)
                    .toList();
        }
    }

    private static List<Filter> parseFilters(String csvFilter) {
        String[] filters = csvFilter.split(",");

        // validate if each entry if it does contains csv have min 2 values..
        if(filters.length < 2) {
            throw new IllegalArgumentException(csvFilter + " is incorrect. Each filter must have at least two values : filter='name,val1,val2'");
        }

        String name = filters[0].trim();

        List<Filter> filterList = new ArrayList<>();
        for (int i = 1; i < filters.length; i++) {
            Filter filter = new Filter(name, filters[i]);
            filterList.add(filter);
        }

        return filterList;
    }

}
