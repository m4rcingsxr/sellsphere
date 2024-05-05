package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 9;
    private final ProductRepository productRepository;


    public ProductPageResponse listProductsPage(ProductPageRequest productPageRequest) {

        Integer pageNum = productPageRequest.getPageNum();

        Integer categoryId = productPageRequest.getCategoryId();
        String keyword = productPageRequest.getKeyword();

        String[] filters = productPageRequest.getFilter();
        BigDecimal maxPrice = productPageRequest.getMaxPrice();
        BigDecimal minPrice = productPageRequest.getMinPrice();

        Sort sort = generateSort(productPageRequest.getSortBy());
        PageRequest pageRequest = PageRequest.of(pageNum, PAGE_SIZE, sort);



        Specification<Product> spec;

        if (keyword != null) {
            if (productPageRequest.getFilter() != null && productPageRequest.getFilter().length > 0 && minPrice != null && maxPrice != null) {
                spec = ProductSpecification.filterProductsByKeywordInPriceBoundaries(
                        keyword,
                        productPageRequest.getFilter(),
                        minPrice,
                        maxPrice
                );
            } else if (productPageRequest.getMinPrice() != null && productPageRequest.getMaxPrice() != null) {
                spec = ProductSpecification.productsByKeywordInPriceBoundaries(
                        keyword, minPrice, maxPrice
                );
            } else {
                spec = ProductSpecifications.hasKeyword(keyword);
            }
        } else if (categoryId != null) {
            if (productPageRequest.getFilter() != null && productPageRequest.getFilter().length > 0 && minPrice != null && maxPrice != null) {
                spec = ProductSpecification.filterProductsByCategoryInPriceBoundaries(
                        categoryId,
                        productPageRequest.getFilter(),
                        minPrice,
                        maxPrice
                );
            } else if (productPageRequest.getMinPrice() != null && productPageRequest.getMaxPrice() != null) {
                spec = ProductSpecification.productsByCategoryInPriceBoundaries(
                        categoryId, minPrice, maxPrice
                );
            } else {
                spec = ProductSpecifications.hasCategory(categoryId);
            }
        } else {
            throw new IllegalStateException("Either categoryId or keyword is required");
        }

        Page<Product> productPage = productRepository.findAll(spec, pageRequest);
        Optional<Product> minProduct = productRepository.findOne(spec.and(ProductSpecifications.minPriceProduct()));
        Optional<Product> maxProduct = productRepository.findOne(spec.and(ProductSpecifications.maxPriceProduct()));

        if (minProduct.isPresent() && maxProduct.isPresent()) {
            maxPrice = maxProduct.get().getDiscountPrice();
            minPrice = minProduct.get().getDiscountPrice();
        }

        return ProductPageResponse.builder().content(
                productPage.map(BasicProductDto::new).toList()).page(
                productPageRequest.getPageNum()).totalElements(
                productPage.getTotalElements()).totalPages(productPage.getTotalPages()).minPrice(
                minPrice).maxPrice(maxPrice).build();
    }

    private Sort generateSort(String sortBy) {
        return switch (ProductSort.valueOf(sortBy)) {
            case LOWEST -> Sort.by(Sort.Direction.ASC, "price");
            case HIGHEST -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.unsorted();
        };
    }

    public Map<String, Map<String, Long>> getAvailableFilterCounts(String keyword,
                                                                   Integer categoryId,
                                                                   String[] filter,
                                                                   BigDecimal minPrice,
                                                                   BigDecimal maxPrice) {
        List<Product> filteredProducts;
        Specification<Product> spec;

        if (keyword != null) {
            if (filter != null && filter.length > 0 && minPrice != null && maxPrice != null) {
                spec = ProductSpecification.filterProductsByKeywordInPriceBoundaries(
                        keyword,
                        filter,
                        minPrice,
                        maxPrice
                );
            } else if (minPrice != null && maxPrice != null) {
                spec = ProductSpecification.productsByKeywordInPriceBoundaries(
                        keyword, minPrice, maxPrice
                );
            } else {
                spec = ProductSpecifications.hasKeyword(keyword);
            }
        } else if (categoryId != null) {
            if (filter != null && filter.length > 0 && minPrice != null && maxPrice != null) {
                spec = ProductSpecification.filterProductsByCategoryInPriceBoundaries(
                        categoryId,
                        filter,
                        minPrice,
                        maxPrice
                );
            } else if (minPrice != null && maxPrice != null) {
                spec = ProductSpecification.productsByCategoryInPriceBoundaries(
                        categoryId, minPrice, maxPrice
                );
            } else {
                spec = ProductSpecifications.hasCategory(categoryId);
            }
        } else {
            throw new IllegalStateException("Either categoryId or keyword is required");
        }

        filteredProducts = productRepository.findAll(spec);

        return getProductMapCountFromProductDetails(filteredProducts);
    }

    // Initialize the map to store counts for both product details and brands
    private static Map<String, Map<String, Long>> getProductMapCountFromProductDetails(
            List<Product> filteredProducts) {

        // Process product details
        Map<String, Map<String, Long>> detailsCounts = filteredProducts.stream().flatMap(
                product -> product.getDetails().stream()).collect(
                Collectors.groupingBy(ProductDetail::getName,
                                      Collectors.groupingBy(ProductDetail::getValue,
                                                            Collectors.counting()
                                      )
                ));

        // Process brand details
        Map<String, Long> brandCounts = filteredProducts.stream().collect(
                Collectors.groupingBy(product -> product.getBrand().getName(),
                                      Collectors.counting()
                ));

        // Add brand counts to the result map
        Map<String, Map<String, Long>> productDetailCounts = new HashMap<>(detailsCounts);
        productDetailCounts.put("Brand", brandCounts);

        return productDetailCounts;
    }

    public Map<String, Map<String, Long>> getAllFilterCounts(FilterMapCountRequest mapRequest) {
        // Retrieve existing counts using getAvailableFilterCounts
        Map<String, Map<String, Long>> counts = getAvailableFilterCounts(mapRequest.getKeyword(),
                                                                         mapRequest.getCategoryId(),
                                                                         mapRequest.getFilter(),
                                                                         mapRequest.getMinPrice(),
                                                                         mapRequest.getMaxPrice()
        );

        String[] filters = mapRequest.getFilter();

        // Retrieve all products matching category or keyword without filters
        List<Product> allProducts;
        Specification<Product> spec;


        if (filters != null && filters.length > 0) {
            if (mapRequest.getMaxPrice() != null && mapRequest.getMinPrice() != null) {
                if (mapRequest.getCategoryId() != null) {

                    // filters + price between + category
                    spec = ProductSpecification.filterProductsByCategoryInPriceBoundaries(
                            mapRequest.getCategoryId(),
                            mapRequest.getFilter(),
                            mapRequest.getMinPrice(),
                            mapRequest.getMaxPrice()
                    );
                } else if (mapRequest.getKeyword() != null) {

                    // filters + price between + keyword
                    spec = ProductSpecification.filterProductsByKeywordInPriceBoundaries(
                            mapRequest.getKeyword(),
                            mapRequest.getFilter(),
                            mapRequest.getMinPrice(),
                            mapRequest.getMaxPrice()
                    );
                } else {
                    throw new IllegalStateException(
                            "Either keyword or categoryID must be provided");
                }
            } else {
                if (mapRequest.getCategoryId() != null) {

                    // filters + category
                    spec = ProductSpecification.filterProductsByCategory(
                            mapRequest.getCategoryId(),
                            mapRequest.getFilter()
                    );
                } else if (mapRequest.getKeyword() != null) {

                    // filters + keyword
                    spec = ProductSpecification.filterProductsByKeyword(
                            mapRequest.getKeyword(),
                            mapRequest.getFilter()
                    );
                } else {
                    throw new IllegalStateException(
                            "Either keyword or categoryID must be provided");
                }
            }


        } else if (mapRequest.getMaxPrice() != null && mapRequest.getMinPrice() != null) {
            if (mapRequest.getCategoryId() != null) {

                // price between + category
                spec = ProductSpecification.productsByCategoryInPriceBoundaries(
                        mapRequest.getCategoryId(),
                        mapRequest.getMinPrice(),
                        mapRequest.getMaxPrice()
                );
            } else if (mapRequest.getKeyword() != null) {

                // price between + keyword
                spec = ProductSpecification.productsByKeywordInPriceBoundaries(
                        mapRequest.getKeyword(),
                        mapRequest.getMinPrice(),
                        mapRequest.getMaxPrice()
                );
            } else {
                throw new IllegalStateException("Either keyword or categoryID must be provided");
            }
        } else {
            return counts;
        }

        allProducts = productRepository.findAll(spec);

        // Initialize counts for all possible product details and brands to zero if not
        // already present
        for (Product product : allProducts) {
            for (ProductDetail detail : product.getDetails()) {
                counts.computeIfAbsent(detail.getName(), k -> new HashMap<>()).putIfAbsent(
                        detail.getValue(), 0L);
            }
            // Initialize brand counts to zero if not already present
            String brandName = product.getBrand().getName();
            counts.computeIfAbsent("Brand", k -> new HashMap<>()).putIfAbsent(brandName, 0L);
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
            sortedEntries.sort(Comparator.<Map.Entry<String, Map<String, Long>>, Boolean>comparing(
                    entry -> !containsFilteredValues(entry.getKey(), entry.getValue(),
                                                     filterSet
                    )).thenComparing(Map.Entry::getKey));
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
                sortedValues.sort(Comparator.<Map.Entry<String, Long>, Boolean>comparing(
                        e -> !filterSet.contains(detailName + "," + e.getKey())).thenComparing(
                        Map.Entry::getKey));
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