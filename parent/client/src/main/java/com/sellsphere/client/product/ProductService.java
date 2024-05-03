package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
        List<Product> filteredProducts = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest));

        return filteredProducts.stream().flatMap(product -> product.getDetails().stream()).collect(
                Collectors.groupingBy(ProductDetail::getName,
                                      Collectors.groupingBy(ProductDetail::getValue,
                                                            Collectors.counting()
                                      )
                ));
    }

    public Map<String, Map<String, Long>> getAllFilterCounts(ProductPageRequest pageRequest) {
        String[] filters = pageRequest.getFilter();

        // Retrieve existing counts using getAvailableFilterCounts
        Map<String, Map<String, Long>> counts = getAvailableFilterCounts(pageRequest);

        // Retrieve all products matching category or keyword without filters
        pageRequest.setFilter(null);
        List<Product> allProducts = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest));

        // Initialize counts for all possible product details to zero if not already present
        for (Product product : allProducts) {
            for (ProductDetail detail : product.getDetails()) {
                counts.computeIfAbsent(detail.getName(), k -> new HashMap<>()).putIfAbsent(
                        detail.getValue(), 0L);
            }
        }

        // Sort the counts
        return sortCounts(counts, filters);
    }

    public Map<String, Map<String, Long>> sortCounts(Map<String, Map<String, Long>> counts,
                                                      String[] filters) {
        // Create a list of entries to sort
        List<Map.Entry<String, Map<String, Long>>> sortedEntries = new ArrayList<>(
                counts.entrySet());

        // Sort the product detail names
        sortedEntries.sort(Comparator
                                   .<Map.Entry<String, Map<String, Long>>, Boolean>comparing(
                                           entry -> !containsFilteredValues(entry.getKey(),
                                                                            entry.getValue(),
                                                                            filters
                                           ))
                                   .thenComparing(Map.Entry::getKey));

        // Sort the values under each product detail name
        Map<String, Map<String, Long>> sortedCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : sortedEntries) {
            String detailName = entry.getKey();
            Map<String, Long> values = entry.getValue();

            List<Map.Entry<String, Long>> sortedValues = new ArrayList<>(values.entrySet());
            sortedValues.sort(Comparator
                                      .<Map.Entry<String, Long>, Boolean>comparing(
                                              e -> !isInFilter(filters, detailName, e.getKey()))
                                      .thenComparing(Map.Entry::getKey));

            Map<String, Long> sortedValueMap = new LinkedHashMap<>();
            for (Map.Entry<String, Long> sortedEntry : sortedValues) {
                sortedValueMap.put(sortedEntry.getKey(), sortedEntry.getValue());
            }

            sortedCounts.put(detailName, sortedValueMap);
        }

        return sortedCounts;
    }

    private boolean isInFilter(String[] filters, String detailName, String value) {
        if (filters == null) {
            return false;
        }
        String filterString = detailName + "," + value;
        for (String filter : filters) {
            if (filter.equals(filterString)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFilteredValues(String detailName, Map<String, Long> values,
                                           String[] filters) {
        if (filters == null) {
            return false;
        }
        for (String value : values.keySet()) {
            if (isInFilter(filters, detailName, value)) {
                return true;
            }
        }
        return false;
    }
}