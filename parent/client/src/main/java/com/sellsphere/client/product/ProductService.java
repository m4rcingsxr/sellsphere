package com.sellsphere.client.product;

import com.sellsphere.common.entity.payload.BasicProductDTO;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import com.sellsphere.common.entity.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for handling product-related operations.
 * Provides methods to filter, sort, and count product details based on user-provided criteria.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 9;
    private final ProductRepository productRepository;

    public Product findByAlias(String alias) throws ProductNotFoundException {
        return productRepository.findByAlias(alias).orElseThrow(ProductNotFoundException::new);
    }

    public Product findById(Integer id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    /**
     * Lists products based on the provided product page request.
     *
     * @param productPageRequest the request object containing filtering, sorting, and pagination
     *                           information.
     * @return a response object containing the list of products and pagination details.
     */
    public ProductPageResponse listProducts(ProductPageRequest productPageRequest) {

        // Create a specification for filtering products based on the request parameters.
        Specification<Product> spec = ProductSpecification.filterProducts(
                productPageRequest.getCategoryId(), productPageRequest.getKeyword(),
                productPageRequest.getFilter(), productPageRequest.getMinPrice(),
                productPageRequest.getMaxPrice()
        );

        // Generate the sort order based on the provided sort parameter.
        Sort sort = generateSort(productPageRequest.getSortBy());
        PageRequest pageRequest = PageRequest.of(productPageRequest.getPageNum(), PAGE_SIZE, sort);

        // Fetch the products page based on the specification and pagination info.
        Page<Product> productPage = productRepository.findAll(spec, pageRequest);

        // Find the minimum and maximum discount prices within the filtered products.
        List<Product> minProduct = productRepository.findAll(
                ProductSpecification.minDiscountPrice(productPageRequest.getCategoryId(),
                                                      productPageRequest.getKeyword(), spec));
        List<Product> maxProduct = productRepository.findAll(
                ProductSpecification.maxDiscountPrice(productPageRequest.getCategoryId(),
                                                      productPageRequest.getKeyword(), spec));

        // Build and return the response containing the products and additional pagination and
        // price details.
        return ProductPageResponse.builder().content(
                productPage.map(BasicProductDTO::new).toList()).page(
                productPageRequest.getPageNum()).totalElements(
                productPage.getTotalElements()).totalPages(productPage.getTotalPages()).minPrice(
                Optional.of(maxProduct.get(0)).map(Product::getDiscountPrice).orElse(
                        productPageRequest.getMinPrice())).maxPrice(
                Optional.of(minProduct.get(0)).map(Product::getDiscountPrice).orElse(
                        productPageRequest.getMinPrice())).build();
    }

    /**
     * Generates a Sort object based on the provided sort parameter.
     *
     * @param sortBy the sort parameter specifying the sorting criteria.
     * @return a Sort object defining the sorting order.
     */
    private Sort generateSort(String sortBy) {
        return switch (ProductSort.valueOf(sortBy)) {
            case LOWEST -> Sort.by(Sort.Direction.ASC, "price");
            case HIGHEST -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.unsorted();
        };
    }

    /**
     * Calculates the available filter counts for product details and brands based on the
     * provided criteria.
     * Excludes values where products do not appear.
     *
     * @param keyword    the keyword for filtering products.
     * @param categoryId the category ID for filtering products.
     * @param filter     the filter criteria for product details.
     * @param minPrice   the minimum price for filtering products.
     * @param maxPrice   the maximum price for filtering products.
     * @return a map containing the counts of available product details and brands.
     */
    public Map<String, Map<String, Long>> calculateAvailableFilterCounts(String keyword,
                                                                         Integer categoryId,
                                                                         String[] filter,
                                                                         BigDecimal minPrice,
                                                                         BigDecimal maxPrice) {

        // Create a specification for filtering products based on the provided criteria.
        Specification<Product> spec = ProductSpecification.filterProducts(categoryId, keyword,
                                                                          filter, minPrice, maxPrice
        );

        // Fetch the filtered products based on the specification.
        List<Product> filteredProducts = productRepository.findAll(spec);

        // Aggregate the product details and return the counts.
        return aggregateProductDetails(filteredProducts);
    }


    /**
     * Aggregates product details and counts their occurrences in the filtered products.
     * Also counts the occurrences of each brand.
     *
     * @param filteredProducts the list of filtered products.
     * @return a map containing the counts of product details and brands.
     */
    private static Map<String, Map<String, Long>> aggregateProductDetails(
            List<Product> filteredProducts) {

        // Group and count product details.
        Map<String, Map<String, Long>> detailsCounts = filteredProducts.stream().flatMap(
                product -> product.getDetails().stream()).collect(
                Collectors.groupingBy(ProductDetail::getName,
                                      Collectors.groupingBy(ProductDetail::getValue,
                                                            Collectors.counting()
                                      )
                ));

        // Group and count brands.
        Map<String, Long> brandCounts = filteredProducts.stream().collect(
                Collectors.groupingBy(product -> product.getBrand().getName(),
                                      Collectors.counting()
                ));

        // Combine product detail counts and brand counts into a single map.
        Map<String, Map<String, Long>> productDetailCounts = new HashMap<>(detailsCounts);
        productDetailCounts.put("Brand", brandCounts);

        return productDetailCounts;
    }

    /**
     * Calculates all filter counts for product details and brands based on the provided criteria.
     * Includes values to display in the UI where products do not appear (counts as 0).
     *
     * @param mapRequest the request object containing filtering criteria.
     * @return a map containing the counts of all product details and brands, including those
     * with zero occurrences.
     */
    public Map<String, Map<String, Long>> calculateAllFilterCounts(
            FilterMapCountRequest mapRequest) {

        // Calculate the available filter counts.
        Map<String, Map<String, Long>> counts = calculateAvailableFilterCounts(
                mapRequest.getKeyword(), mapRequest.getCategoryId(), mapRequest.getFilter(),
                mapRequest.getMinPrice(), mapRequest.getMaxPrice()
        );

        // Create a specification for filtering products based on the provided criteria.
        // Even if filters was provided we need to ensure that we retrieve all the products to
        // include 0
        Specification<Product> spec = ProductSpecification.hasCategoryAndKeyword(
                mapRequest.getCategoryId(),
                mapRequest.getKeyword()
        );
        List<Product> allProducts = productRepository.findAll(spec);


        // Ensure all possible product details and brands are included in the counts map,
        // initializing counts to zero if not already present.
        for (Product product : allProducts) {
            for (ProductDetail detail : product.getDetails()) {
                counts.computeIfAbsent(detail.getName(), k -> new HashMap<>()).putIfAbsent(
                        detail.getValue(), 0L);
            }
            String brandName = product.getBrand().getName();
            counts.computeIfAbsent("Brand", k -> new HashMap<>()).putIfAbsent(brandName, 0L);
        }

        // Sort the filter counts and return the sorted map.
        return sortFilterCounts(counts, mapRequest.getFilter());
    }

    /**
     * Sorts the filter counts first by whether the user has checked the product detail, then by
     * product detail name.
     *
     * @param counts  the map of filter counts to be sorted.
     * @param filters the array of filters provided by the user.
     * @return a sorted map of filter counts.
     */
    private Map<String, Map<String, Long>> sortFilterCounts(Map<String, Map<String, Long>> counts,
                                                            String[] filters) {
        Set<String> filterSet = filters != null ? new HashSet<>(Arrays.asList(filters)) : null;

        // Convert the counts map to a list of entries for sorting.
        List<Map.Entry<String, Map<String, Long>>> sortedEntries = new ArrayList<>(
                counts.entrySet());

        // Sort the entries first by whether the user has checked the product detail, then by
        // product detail name.
        if (filterSet != null) {
            sortedEntries.sort(Comparator.<Map.Entry<String, Map<String, Long>>, Boolean>comparing(
                    entry -> !containsFilteredValues(entry.getKey(), entry.getValue(),
                                                     filterSet
                    )).thenComparing(Map.Entry::getKey));
        } else {
            sortedEntries.sort(Map.Entry.comparingByKey());
        }

        // Create a sorted map from the sorted entries.
        Map<String, Map<String, Long>> sortedCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : sortedEntries) {
            List<Map.Entry<String, Long>> sortedValues = new ArrayList<>(
                    entry.getValue().entrySet());

            // Sort the values under each product detail name.
            if (filterSet != null) {
                sortedValues.sort(Comparator.<Map.Entry<String, Long>, Boolean>comparing(
                        e -> !filterSet.contains(entry.getKey() + "," + e.getKey())).thenComparing(
                        Map.Entry::getKey));
            } else {
                sortedValues.sort(Map.Entry.comparingByKey());
            }

            Map<String, Long> sortedValueMap = new LinkedHashMap<>();
            for (Map.Entry<String, Long> sortedEntry : sortedValues) {
                sortedValueMap.put(sortedEntry.getKey(), sortedEntry.getValue());
            }

            sortedCounts.put(entry.getKey(), sortedValueMap);
        }

        return sortedCounts;
    }

    /**
     * Checks if the filter set contains any of the values for the given detail name.
     *
     * @param detailName the name of the product detail.
     * @param values     the map of values for the product detail.
     * @param filterSet  the set of filters provided by the user.
     * @return true if the filter set contains any of the values for the given detail name, false
     * otherwise.
     */
    private boolean containsFilteredValues(String detailName, Map<String, Long> values,
                                           Set<String> filterSet) {
        for (String value : values.keySet()) {
            if (filterSet.contains(detailName + "," + value)) {
                return true;
            }
        }
        return false;
    }

    public List<Product> getProductsByIds(List<Integer> productIds) {
        return this.productRepository.findAllById(productIds);
    }
}