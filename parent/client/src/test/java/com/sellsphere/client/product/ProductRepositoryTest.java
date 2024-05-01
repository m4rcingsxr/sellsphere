package com.sellsphere.client.product;

import com.sellsphere.client.PageUtil;
import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/products.sql")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void givenEmptyFiltersAndCategory_whenFindAll_thenReturnPagedProductsSortedByName() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{}, "laptops", null, 0);
        pageRequest.setCategoryId(1);
        Specification<Product> spec = ProductSpecification.filterProducts(pageRequest);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());

        // When
        var result = productRepository.findAll(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent().size()).isEqualTo(2);
        PageUtil.assertSortOrder(result.getContent(), Comparator.comparing(Product::getName));
    }

    @Test
    void givenEmptyFiltersAndKeyword_whenFindAll_thenReturnPagedProductsSortedByPrice() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{}, null, "Laptop", 0);
        Specification<Product> spec = ProductSpecification.filterProducts(pageRequest);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());

        // When
        var result = productRepository.findAll(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().size()).isEqualTo(2);
        PageUtil.assertSortOrder(result.getContent(), Comparator.comparing(Product::getPrice));
    }

    @Test
    void givenBrandFilterAndCategory_whenFindAll_thenReturnPagedProductsSortedByPrice() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{"brand,Apple"}, "laptops", null, 0);
        Specification<Product> spec = ProductSpecification.filterProducts(pageRequest);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());

        // When
        var result = productRepository.findAll(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().size()).isEqualTo(1);
        PageUtil.assertSortOrder(result.getContent(), Comparator.comparing(Product::getPrice));
    }

    @Test
    void givenFeatureFilterAndKeyword_whenFindAll_thenReturnPagedProductsSortedByName() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{"Processor,Intel Core i7-8565U"}, null, "Laptop", 0);
        Specification<Product> spec = ProductSpecification.filterProducts(pageRequest);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());

        // When
        var result = productRepository.findAll(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().size()).isEqualTo(1);
        PageUtil.assertSortOrder(result.getContent(), Comparator.comparing(Product::getName));
    }

    @Test
    void givenMultipleFiltersAndCategory_whenFindAll_thenReturnPagedProductsSortedByPrice() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{
                "RAM,16GB DDR4", "Storage,1TB SSD"}, "laptops", null, 0);
        Specification<Product> spec = ProductSpecification.filterProducts(pageRequest);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());

        // When
        var result = productRepository.findAll(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().size()).isEqualTo(2);
        PageUtil.assertSortOrder(result.getContent(), Comparator.comparing(Product::getPrice));
    }


    @Test
    void givenInvalidFilterFormat_whenFindAll_thenThrowIllegalArgumentException() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{"invalidFilterFormat"}, "laptops", null, 0);
        Pageable pageable = PageRequest.of(0, 2);

        // When & Then
        assertThatThrownBy(() -> productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("is incorrect. Each filter must have two values : filter='name,value'");
    }

    @Test
    void givenEmptyFilters_whenFindAll_thenThrowIllegalArgumentException() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{}, null, null, 0);
        Pageable pageable = PageRequest.of(0, 2);

        // When & Then
        assertThatThrownBy(() -> productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("Either category alias or keyword must be provided");
    }

    @Test
    void givenFiltersWithMissingValues_whenFindAll_thenThrowIllegalArgumentException() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{"brand"}, "laptops", null, 0);
        Pageable pageable = PageRequest.of(0, 2);

        // When & Then
        assertThatThrownBy(() -> productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("is incorrect. Each filter must have two values : filter='name,value'");
    }

    @Test
    void givenFiltersWithEmptyName_whenFindAll_thenThrowIllegalArgumentException() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest(new String[]{",value"}, "laptops", null, 0);
        Pageable pageable = PageRequest.of(0, 2);

        // When & Then
        assertThatThrownBy(() -> productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("Filter name and value cannot be empty.");
    }

}
