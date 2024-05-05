//package com.sellsphere.client.product;
//
//import com.sellsphere.common.entity.Product;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.jdbc.Sql;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/products.sql")
//class ProductRepositoryTest {
//
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @ParameterizedTest
//    @CsvSource({"'brand,Apple|Color,Red', 'Product One,Product Two'",
//                "'brand,Apple|Size,Medium', 'Product One,Product Two'",
//                "'brand,Apple|Weight,1.5kg', 'Product One'",
//                "'brand,Apple|Material,Plastic', 'Product Two'",
//                "'brand,Apple|Color,Green', 'Product Three'",
//                "'brand,Apple|Size,Large', 'Product Three'", "'brand,Apple|Color,Black', ''",
//                "'brand,Apple|Size,Small', ''", "'brand,Apple|Warranty,3 years', ''",
//                "'brand,Apple|Warranty,5 years', ''",
//                "'brand,Apple|Color,Red|Size,Medium', 'Product One,Product Two'",
//                "'brand,Apple|Color,Green|Weight,2kg', 'Product Three'"})
//    void givenFilters_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
//            String filter, String expectedProducts) {
//        // Prepare the filters
//        String[] filters = filter.split("\\|");
//        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null,null,null, 0);
//        pageRequest.setCategoryId(1);
//
//        // Get the filtered products
//        List<Product> result = productRepository.findAll(
//                ProductSpecification.filterProducts(pageRequest));
//
//        // Convert expected products to a list
//        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(
//                expectedProducts.split(","));
//
//        // Assert the result
//        List<String> resultNames = result.stream().map(Product::getName).collect(
//                Collectors.toList());
//        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
//    }
//
//
//    @ParameterizedTest
//    @CsvSource({
//            "'brand,Apple|Color,Red', 'Product One', 2, 1, 2",
//            "'brand,Apple|Size,Medium', 'Product One', 2, 1, 2",
//            "'brand,Apple|Weight,1.5kg', 'Product One', 1, 1, 1",
//            "'brand,Apple|Material,Plastic', 'Product Two', 1, 1, 1",
//            "'brand,Apple|Color,Green', 'Product Three', 1, 1, 1",
//            "'brand,Apple|Size,Large', 'Product Three', 1, 1, 1",
//            "'brand,Apple|Color,Black', '', 0, 0, 0",
//            "'brand,Apple|Size,Small', '', 0, 0, 0",
//            "'brand,Apple|Warranty,3 years', '', 0, 0, 0",
//            "'brand,Apple|Warranty,5 years', '', 0, 0, 0",
//            "'brand,Apple|Color,Red|Size,Medium', 'Product One', 2, 1, 2",
//            "'brand,Apple|Color,Green|Weight,2kg', 'Product Three', 1, 1, 1"
//    })
//    void givenFilters_whenFindAllWithProductSpecificationAndPagination_thenShouldReturnMatchingFilterExpectedProductsWithPagination(
//            String filter, String expectedProducts, long totalElements, int contentSize, int totalPages) {
//        // Prepare the filters
//        String[] filters = filter.split("\\|");
//        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null,null,null, 0);
//        pageRequest.setCategoryId(1);
//
//        // Set page size to 1
//        PageRequest pageable = PageRequest.of(0, 1);
//
//        // Get the filtered products with pagination
//        Page<Product> resultPage = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);
//
//        // Convert expected products to a list
//        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));
//
//        // Assert the result
//        List<String> resultNames = resultPage.getContent().stream().map(Product::getName).collect(Collectors.toList());
//        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
//
//        // Assert the page information
//        assertThat(resultPage.getTotalElements()).isEqualTo(totalElements);
//        assertThat(resultPage.getContent().size()).isEqualTo(contentSize);
//        assertThat(resultPage.getTotalPages()).isEqualTo(totalPages);
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//            "'brand,Apple|Color,Red', 10, 150, 'Product One,Product Two'",
//            "'brand,Apple|Color,Red', 10, 20, 'Product One,Product Two'",
//            "'brand,Apple|Size,Medium', 10, 20, 'Product One,Product Two'",
//            "'brand,Apple|Weight,1.5kg', 10, 20, 'Product One'",
//            "'brand,Apple|Material,Plastic', 10, 40, 'Product Two'",
//            "'brand,Apple|Color,Green', 30, 50, 'Product Three'",
//            "'brand,Apple|Size,Large', 30, 50, 'Product Three'",
//            "'brand,Apple|Color,Black', 10, 20, ''",
//            "'brand,Apple|Size,Small', 10, 20, ''",
//            "'brand,Apple|Warranty,3 years', 10, 50, ''",
//            "'brand,Apple|Warranty,5 years', 10, 50, ''",
//            "'brand,Apple|Color,Red|Size,Medium', 10, 20, 'Product One,Product Two'",
//            "'brand,Apple|Color,Green|Weight,2kg', 30, 50, 'Product Three'"
//    })
//    void givenFiltersAndPriceRange_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
//            String filter, BigDecimal minPrice, BigDecimal maxPrice, String expectedProducts) {
//        // Prepare the filters
//        String[] filters = filter.split("\\|");
//        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops",null, minPrice, maxPrice, 0);
//        pageRequest.setCategoryId(1);
//
//        // Get the filtered products
//        List<Product> result = productRepository.findAll(
//                ProductSpecification.filterProducts(pageRequest));
//
//        // Convert expected products to a list
//        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(
//                expectedProducts.split(","));
//
//        // Assert the result
//        List<String> resultNames = result.stream().map(Product::getName).collect(
//                Collectors.toList());
//        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
//    }
//
//}