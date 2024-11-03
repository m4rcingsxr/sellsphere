package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("mysql_test")
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @ParameterizedTest
    @CsvSource({
            "'Color,Red', 'Canon EOS 90D,Fujifilm X-T30'",
            "'Size,Medium', 'Canon EOS 90D,Fujifilm X-T30'",
            "'Weight,1.5kg', 'Canon EOS 90D,Fujifilm X-T30'",
            "'Color,Green', 'Sony A7R IV'",
            "'Size,Large', 'Sony A7R IV'",
            "'Warranty,3 years', 'Sony A7R IV'",
            "'Color,Black', 'HP Pavilion Gaming Desktop'",
            "'Warranty,5 years', 'SanDisk Ultra SSD'",
            "'Color,Red|Size,Medium', 'Canon EOS 90D,Fujifilm X-T30'",
            "'Warranty,1 year', 'Canon EOS 90D,Fujifilm X-T30'",
            "'Warranty,6 months', 'HP Pavilion Gaming Desktop'",
    })
    void givenSimpleFilters_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
            String filter, String expectedProducts) {
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "electronics", null, null, null, 0);
        pageRequest.setCategoryId(1);

        List<Product> result = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice())
        );

        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }

    @ParameterizedTest
    @CsvSource({
            "'Color,Red', 'Canon EOS 90D', 2, 1, 2",
            "'Size,Medium', 'Canon EOS 90D', 2, 1, 2",
            "'Weight,1.5kg', 'Canon EOS 90D', 2, 1, 2",
            "'Color,Red|Size,Medium', 'Canon EOS 90D', 2, 1, 2",
            "'Color,Green', 'Sony A7R IV', 1, 1, 1",
            "'Size,Large', 'Sony A7R IV', 1, 1, 1",
            "'Warranty,3 years', 'Sony A7R IV', 1, 1, 1",
            "'Color,Black', 'HP Pavilion Gaming Desktop', 1, 1, 1",
            "'Warranty,5 years', 'SanDisk Ultra SSD', 1, 1, 1"
    })
    void givenFiltersWithPagination_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProductsWithPagination(
            String filter, String expectedProducts, long totalElements, int contentSize, int totalPages) {
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "electronics", null, null, null, 0);
        PageRequest pageable = PageRequest.of(0, 1);

        Page<Product> resultPage = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice()),
                pageable
        );

        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));
        List<String> resultNames = resultPage.getContent().stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);

        assertThat(resultPage.getTotalElements()).isEqualTo(totalElements);
        assertThat(resultPage.getContent().size()).isEqualTo(contentSize);
        assertThat(resultPage.getTotalPages()).isEqualTo(totalPages);
    }

    @ParameterizedTest
    @CsvSource({
            "'Color,Red', 800, 1300, 'Canon EOS 90D,Fujifilm X-T30'",
            "'Color,Green', 2000, 4000, 'Sony A7R IV'",
            "'Size,Large', 2000, 4000, 'Sony A7R IV'",
            "'Warranty,3 years', 2000, 4000, 'Sony A7R IV'"
    })
    void givenFiltersAndPriceRange_whenFindAllWithProductSpecification_thenShouldReturnMatchingFilterExpectedProducts(
            String filter, BigDecimal minPrice, BigDecimal maxPrice, String expectedProducts) {
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "electronics", null, minPrice, maxPrice, 0);
        pageRequest.setCategoryId(1);

        List<Product> result = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest.getCategoryId(), pageRequest.getKeyword(), pageRequest.getFilter(), pageRequest.getMinPrice(), pageRequest.getMaxPrice())
        );

        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 'desktop', 'HP Pavilion Gaming Desktop'",
            "9, 'SSD', 'SanDisk Ultra SSD'"
    })
    void givenCategoryOrKeyword_whenFindAllWithCategoryOrKeyword_thenShouldReturnMatchingFilterExpectedProducts(
            Integer categoryId, String keyword, String expectedProducts) {
        keyword = keyword.isEmpty() ? null : keyword;
        categoryId = (categoryId == null || categoryId == 0) ? null : categoryId;

        List<Product> result = productRepository.findAll(
                ProductSpecification.hasCategoryAndKeyword(categoryId, keyword)
        );

        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));
        List<String> resultNames = result.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);
    }
}
