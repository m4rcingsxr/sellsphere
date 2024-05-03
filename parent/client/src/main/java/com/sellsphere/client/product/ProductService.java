package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;


    public ProductPageResponse listProductsPage(ProductPageRequest pageRequest) {
        PageRequest pageable = PageRequest.of(pageRequest.getPageNum(), PAGE_SIZE);
        Page<Product> products = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest), pageable);

        return ProductPageResponse.builder().content(
                products.map(BasicProductDto::new).toList()).page(
                pageRequest.getPageNum()).totalElements(products.getTotalElements()).totalPages(
                products.getTotalPages()).pageSize(PAGE_SIZE).build();
    }

    public Map<String, Map<String, Long>> getAvailableFilterCounts(ProductPageRequest pageRequest) {
        List<Product> filteredProducts = productRepository.findAll(ProductSpecification.filterProducts(pageRequest));

        return getProductCountFromProductDetail(filteredProducts);
    }

    private static Map<String, Map<String, Long>> getProductCountFromProductDetail(
            List<Product> filteredProducts) {
        return filteredProducts.stream().flatMap(product -> product.getDetails().stream()).collect(
                Collectors.groupingBy(ProductDetail::getName,
                                      Collectors.groupingBy(ProductDetail::getValue,
                                                            Collectors.counting()
                                      )
                ));
    }

    public Map<String, Map<String, Long>> getAllFilterCounts(ProductPageRequest pageRequest) {

        // Retrieve existing counts using getAvailableFilterCounts
        Map<String, Map<String, Long>> counts = getAvailableFilterCounts(pageRequest);

        String[] filters = pageRequest.getFilter();

        // Retrieve all products matching category or keyword without filters
        if (filters != null && filters.length > 0) {
            pageRequest.setFilter(null);
            List<Product> allProducts = productRepository.findAll(ProductSpecification.hasCategoryOrKeyword(pageRequest));

            // Initialize counts for all possible product details to zero if not already present
            for (Product product : allProducts) {
                for (ProductDetail detail : product.getDetails()) {
                    counts.computeIfAbsent(detail.getName(), k -> new HashMap<>()).putIfAbsent(
                            detail.getValue(), 0L);
                }
            }
        }

        // Sort the counts
        return sortCounts(counts, filters);
    }


    public Map<String, Map<String, Long>> sortCounts(Map<String, Map<String, Long>> counts,
                                                     String[] filters) {
        Set<String> filterSet = filters != null ? new HashSet<>(Arrays.asList(filters)) : null;

        // Create a list of entries to sort
        List<Map.Entry<String, Map<String, Long>>> sortedEntries = new ArrayList<>(
                counts.entrySet());

        // Sort the product detail names
        if (filterSet != null) {
            sortedEntries.sort(Comparator
                                       .<Map.Entry<String, Map<String, Long>>, Boolean>comparing(
                                               entry -> !containsFilteredValues(entry.getKey(),
                                                                                entry.getValue(),
                                                                                filterSet
                                               ))
                                       .thenComparing(Map.Entry::getKey));
        } else {
            sortedEntries.sort(Map.Entry.comparingByKey());
        }

        // Sort the values under each product detail name
        Map<String, Map<String, Long>> sortedCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : sortedEntries) {
            String detailName = entry.getKey();
            Map<String, Long> values = entry.getValue();

            List<Map.Entry<String, Long>> sortedValues = new ArrayList<>(values.entrySet());
            if (filterSet != null) {
                sortedValues.sort(Comparator
                                          .<Map.Entry<String, Long>, Boolean>comparing(
                                                  e -> !filterSet.contains(
                                                          detailName + "," + e.getKey()))
                                          .thenComparing(Map.Entry::getKey));
            } else {
                sortedValues.sort(Map.Entry.comparingByKey());
            }

            Map<String, Long> sortedValueMap = new LinkedHashMap<>();
            for (Map.Entry<String, Long> sortedEntry : sortedValues) {
                sortedValueMap.put(sortedEntry.getKey(), sortedEntry.getValue());
            }

            sortedCounts.put(detailName, sortedValueMap);
        }

        return sortedCounts;
    }

    private boolean containsFilteredValues(String detailName, Map<String, Long> values,
                                           Set<String> filterSet) {
        for (String value : values.keySet()) {
            if (filterSet.contains(detailName + "," + value)) {
                return true;
            }
        }
        return false;
    }
}