package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;

    private final EntityManager entityManager;


    public ProductPageResponse listProductsPage(ProductPageRequest pageRequest) {
        PageRequest pageable = PageRequest.of(pageRequest.getPageNum(), PAGE_SIZE);
        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest), pageable);

        BigDecimal minPrice = getMinPrice(pageRequest).orElse(pageRequest.getMinPrice());
        BigDecimal maxPrice = getMaxPrice(pageRequest).orElse(pageRequest.getMaxPrice());

        return ProductPageResponse.builder().content(
                productPage.map(BasicProductDto::new).toList()).page(
                pageRequest.getPageNum()).totalElements(productPage.getTotalElements()).totalPages(
                productPage.getTotalPages()).pageSize(PAGE_SIZE)
                .minPrice(minPrice).maxPrice(maxPrice).build();
    }

    private Optional<BigDecimal> getMinPrice(ProductPageRequest pageRequest) {
        Specification<Product> spec = ProductSpecification.minPriceProduct(pageRequest);

        return getPriceBySpecification(spec);
    }

    private Optional<BigDecimal> getMaxPrice(ProductPageRequest pageRequest) {
        Specification<Product> spec = ProductSpecification.maxPriceProduct(pageRequest);

        return getPriceBySpecification(spec);
    }

    private Optional<BigDecimal> getPriceBySpecification(Specification<Product> spec) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = criteriaBuilder.createQuery(BigDecimal.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        // Calculate discounted price
        Expression<BigDecimal> discountedPrice = criteriaBuilder.diff(
                root.get("price"),
                criteriaBuilder.prod(
                        root.get("price"),
                        criteriaBuilder.quot(root.get("discountPercent"), BigDecimal.valueOf(100))
                ).as(BigDecimal.class)
        ).as(BigDecimal.class);

        criteriaQuery.select(discountedPrice);
        criteriaQuery.where(spec.toPredicate(root, criteriaQuery, criteriaBuilder));

        try {
            return Optional.of(
                    entityManager.createQuery(criteriaQuery)
                            .setMaxResults(1)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    public Map<String, Map<String, Long>> getAvailableFilterCounts(ProductPageRequest pageRequest) {
        List<Product> filteredProducts = productRepository.findAll(ProductSpecification.filterProducts(pageRequest));

        return getProductCountFromProductDetail(filteredProducts);
    }

    private static Map<String, Map<String, Long>> getProductCountFromProductDetail(List<Product> filteredProducts) {
        // Initialize the map to store counts for both product details and brands

        // Process product details
        Map<String, Map<String, Long>> detailsCounts = filteredProducts.stream()
                .flatMap(product -> product.getDetails().stream())
                .collect(Collectors.groupingBy(ProductDetail::getName,
                                               Collectors.groupingBy(ProductDetail::getValue, Collectors.counting())));

        // Process brand details
        Map<String, Long> brandCounts = filteredProducts.stream()
                .collect(Collectors.groupingBy(product -> product.getBrand().getName(), Collectors.counting()));

        // Add brand counts to the result map
        Map<String, Map<String, Long>> productDetailCounts = new HashMap<>(detailsCounts);
        productDetailCounts.put("Brand", brandCounts);

        return productDetailCounts;
    }

    public Map<String, Map<String, Long>> getAllFilterCounts(ProductPageRequest pageRequest) {
        // Retrieve existing counts using getAvailableFilterCounts
        Map<String, Map<String, Long>> counts = getAvailableFilterCounts(pageRequest);

        String[] filters = pageRequest.getFilter();

        // Retrieve all products matching category or keyword without filters
        if ((filters != null && filters.length > 0) || (pageRequest.getMaxPrice() != null && pageRequest.getMinPrice() != null)) {
            pageRequest.setFilter(null);
            List<Product> allProducts = productRepository.findAll(ProductSpecification.hasCategoryOrKeyword(pageRequest));

            // Initialize counts for all possible product details and brands to zero if not already present
            for (Product product : allProducts) {
                for (ProductDetail detail : product.getDetails()) {
                    counts.computeIfAbsent(detail.getName(), k -> new HashMap<>()).putIfAbsent(
                            detail.getValue(), 0L);
                }
                // Initialize brand counts to zero if not already present
                String brandName = product.getBrand().getName();
                counts.computeIfAbsent("Brand", k -> new HashMap<>()).putIfAbsent(brandName, 0L);
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