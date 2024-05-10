package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("mysql_test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {"classpath:sql/products.sql"})
class ProductRepositoryTest {


    @Autowired
    private ProductRepository productRepository;

    @ParameterizedTest
    @CsvSource({
            "'Brand,Apple|Color,Red', 'Product One,Product Two'",
            "'Brand,Apple|Size,Medium', 'Product One,Product Two'",
            "'Brand,Apple|Weight,1.5kg', 'Product One'",
            "'Brand,Apple|Material,Plastic', 'Product Two'",
            "'Brand,Apple|Color,Green', 'Product Three'",
            "'Brand,Apple|Size,Large', 'Product Three'",
            "'Brand,Apple|Color,Black', ''",
            "'Brand,Apple|Size,Small', ''",
            "'Brand,Apple|Warranty,3 years', ''",
            "'Brand,Apple|Warranty,5 years', ''",
            "'Brand,Apple|Color,Red|Size,Medium', 'Product One,Product Two'",
            "'Brand,Apple|Color,Green|Weight,2kg', 'Product Three'"
    })
    void givenSimpleFilters_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
            String filter, String expectedProducts) {
        // Prepare the filters
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null, null, null, 0);
        pageRequest.setCategoryId(1);

        // Get the filtered products
        List<Product> result = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice())
        );

        // Convert expected products to a list
        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));

        // Assert the result
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }

    @ParameterizedTest
    @CsvSource({
            "'Brand,Apple|Color,Red', 'Product One', 2, 1, 2",
            "'Brand,Apple|Size,Medium', 'Product One', 2, 1, 2",
            "'Brand,Apple|Weight,1.5kg', 'Product One', 1, 1, 1",
            "'Brand,Apple|Material,Plastic', 'Product Two', 1, 1, 1",
            "'Brand,Apple|Color,Green', 'Product Three', 1, 1, 1",
            "'Brand,Apple|Size,Large', 'Product Three', 1, 1, 1",
            "'Brand,Apple|Color,Black', '', 0, 0, 0",
            "'Brand,Apple|Size,Small', '', 0, 0, 0",
            "'Brand,Apple|Warranty,3 years', '', 0, 0, 0",
            "'Brand,Apple|Warranty,5 years', '', 0, 0, 0",
            "'Brand,Apple|Color,Red|Size,Medium', 'Product One', 2, 1, 2",
            "'Brand,Apple|Color,Green|Weight,2kg', 'Product Three', 1, 1, 1"
    })
    void givenFiltersWithPagination_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProductsWithPagination(
            String filter, String expectedProducts, long totalElements, int contentSize, int totalPages) {
        // Prepare the filters
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null, null, null, 0);

        // Set page size to 1
        PageRequest pageable = PageRequest.of(0, 1);

        // Get the filtered products with pagination
        Page<Product> resultPage = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice()),
                pageable
        );

        // Convert expected products to a list
        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));

        // Assert the result
        List<String> resultNames = resultPage.getContent().stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);

        // Assert the page information
        assertThat(resultPage.getTotalElements()).isEqualTo(totalElements);
        assertThat(resultPage.getContent().size()).isEqualTo(contentSize);
        assertThat(resultPage.getTotalPages()).isEqualTo(totalPages);
    }

    @ParameterizedTest
    @CsvSource({
            "'Brand,Apple|Color,Red', 10, 150, 'Product One,Product Two'",
            "'Brand,Apple|Weight,1.5kg', 10, 20, 'Product One'",
            "'Brand,Apple|Material,Plastic', 10, 40, 'Product Two'",
            "'Brand,Apple|Color,Green', 30, 50, 'Product Three'",
            "'Brand,Apple|Size,Large', 30, 50, 'Product Three'",
            "'Brand,Apple|Color,Black', 10, 20, ''",
            "'Brand,Apple|Size,Small', 10, 20, ''",
            "'Brand,Apple|Warranty,3 years', 10, 50, ''",
            "'Brand,Apple|Warranty,5 years', 10, 50, ''",
            "'Brand,Apple|Color,Green|Weight,2kg', 30, 50, 'Product Three'"
    })
    void givenFiltersAndPriceRange_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
            String filter, BigDecimal minPrice, BigDecimal maxPrice, String expectedProducts) {
        // Prepare the filters
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null, minPrice, maxPrice, 0);
        pageRequest.setCategoryId(1);

        // Get the filtered products
        List<Product> result = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice())
        );

        // Convert expected products to a list
        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));

        // Assert the result
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }

    @ParameterizedTest
    @CsvSource({
            "1, '', 'Product One,Product Two,Product Three,Product Four,Product Five,Product Six,Product Seven,Product Eight,Product Nine,Product Ten'",
            ", 'laptops', ''",
            "2, '', 'Product Two,Product Three,Product Seven'",
            ", 'description', 'Product One,Product Two,Product Three,Product Four,Product Five,Product Six,Product Seven,Product Eight,Product Nine,Product Ten'",
            ", 'Short description of Product', 'Product One,Product Two,Product Three,Product Four,Product Five,Product Six,Product Seven,Product Eight,Product Nine,Product Ten'",
            "1, 'detailed description', 'Product One,Product Two,Product Three,Product Four,Product Five,Product Six,Product Seven,Product Eight,Product Nine,Product Ten'",
    })
    void givenCategoryOrKeyword_whenFindAllWithCategoryOrKeyword_thenShouldReturnMatchingFilterExpectedProducts(
            Integer categoryId, String keyword, String expectedProducts) {
        // Prepare inputs
        keyword = keyword.isEmpty() ? null : keyword;
        categoryId = (categoryId == null || categoryId == 0) ? null : categoryId;

        // Get the filtered products
        List<Product> result = productRepository.findAll(
                ProductSpecification.hasCategoryAndKeyword(categoryId, keyword)
        );

        // Convert expected products to a list
        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));

        // Assert the result
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }

    @ParameterizedTest
    @CsvSource({
            "'Brand,Apple|Color,Red', 10, 20, 'Product One'",
            "'Brand,Apple|Color,Red', 10, 20, 'Product One'",
            "'Brand,Apple|Size,Medium', 10, 30, 'Product One,Product Two'",
            "'Brand,Apple|Weight,1.5kg', 10, 20, 'Product One'",
            "'Brand,Apple|Weight,1.5kg', 10, 50, 'Product One'",
            "'Brand,Apple|Color,Green', 10, 40, 'Product Three'",
            "'Brand,Apple|Size,Large', 10, 40, 'Product Three'",
            "'Brand,Samsung|Color,Red', 10, 50, 'Product Four'",
            "'Brand,Samsung|Material,Metal', 10, 50, 'Product Four'",
            "'Brand,Samsung|Warranty,3 years', 10, 70, 'Product Six'",
            "'Brand,Samsung|Warranty,5 years', 10, 80, 'Product Eight'",
            "'Brand,Samsung|Material,Plastic', 10, 80, 'Product Six,Product Eight'",
            "'Brand,Samsung|Color,Black', 10, 60, 'Product Five'",
            "'Brand,Samsung|Size,Small', 10, 60, 'Product Five'",
            "'Brand,Samsung|Warranty,6 months', 10, 80, 'Product Ten'",
            "'Brand,Samsung|Color,Blue', 10, 80, 'Product Eight'"
    })
    void givenFiltersAndPriceRangeAndMinMax_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
            String filter, BigDecimal minPrice, BigDecimal maxPrice, String expectedProducts) {
        // Prepare the filters
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null, minPrice, maxPrice, 0);
        pageRequest.setCategoryId(1);

        // Get the filtered products with base specification
        Specification<Product> baseSpec = ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice());

        List<Product> result = productRepository.findAll(baseSpec);

        // Convert expected products to a list
        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));

        // Assert the result
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }


}