package com.sellsphere.client.product;

import com.sellsphere.client.PageUtil;
import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;
import java.util.List;

import static com.sellsphere.client.PageUtil.assertSortOrder;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/products.sql")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private ProductPageRequest pageRequest;

    @BeforeEach
    void setUp() {
        pageRequest = new ProductPageRequest();
    }

    @Test
    void givenNoCategoryOrKeyword_whenFilteringProducts_thenThrowException() {
        pageRequest.setFilter(new String[]{"Processor,Intel i7"});

        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () ->
                productRepository.findAll(ProductSpecification.filterProducts(pageRequest), Pageable.unpaged()));
        assertTrue(exception.getMessage().contains("Either category alias or keyword must be provided"));
    }

    @Test
    void givenBrandAndCategory_whenFilteringProducts_thenReturnMatchingProducts() {
        pageRequest.setFilter(new String[]{"brand,Apple"});
        pageRequest.setCategoryAlias("electronics");
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(2, products.getTotalElements());
        assertEquals("Apple", products.getContent().get(0).getBrand().getName());
        assertEquals("Apple", products.getContent().get(1).getBrand().getName());
    }

    @Test
    void givenFiltersAndCategory_whenFilteringProducts_thenReturnMatchingProducts() {
        pageRequest.setFilter(new String[]{"Processor,Intel i7", "RAM,16GB"});
        pageRequest.setCategoryAlias("electronics");
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(1, products.getTotalElements());
        assertEquals("MacBook Pro", products.getContent().get(0).getName());
    }

    @Test
    void givenKeyword_whenFilteringProducts_thenReturnMatchingProducts() {
        pageRequest.setKeyword("MacBook");
        pageRequest.setFilter(new String[]{"Processor,Intel i7"});
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(1, products.getTotalElements());
        assertEquals("MacBook Pro", products.getContent().get(0).getName());
    }

    @Test
    void givenComplexFiltersAndCategory_whenFilteringProducts_thenReturnMatchingProducts() {
        pageRequest.setFilter(new String[]{"brand,Apple", "Processor,Intel i7", "RAM,16GB"});
        pageRequest.setCategoryAlias("electronics");
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(1, products.getTotalElements());
        assertEquals("MacBook Pro", products.getContent().get(0).getName());
    }

    @Test
    void givenComplexFiltersAndKeyword_whenFilteringProducts_thenReturnMatchingProducts() {
        pageRequest.setKeyword("AMD");
        pageRequest.setFilter(new String[]{"GPU,AMD Radeon RX 6800"});
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(1, products.getTotalElements());
        assertEquals("AMD Radeon RX 6800", products.getContent().get(0).getName());
    }

    @Test
    void givenSortFieldAndDirection_whenFilteringProducts_thenReturnSortedResults() {
        pageRequest.setCategoryAlias("electronics");
        pageRequest.setFilter(new String[]{"brand,Apple"});
        Pageable pageable = PageRequest.of(0, 10, PageUtil.generateSort("price", "desc"));

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(2, products.getTotalElements());

        List<Product> productList = products.getContent();
        assertSortOrder(productList, Comparator.comparing(Product::getPrice).reversed());
    }

    @Test
    void givenPagedRequest_whenFilteringProducts_thenReturnCorrectPage() {
        pageRequest.setCategoryAlias("electronics");
        pageRequest.setFilter(new String[]{"brand,Apple"});
        Pageable pageable = PageRequest.of(0, 1, PageUtil.generateSort("price", "desc"));

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(1, products.getNumberOfElements());
        assertEquals(2, products.getTotalElements());
        assertEquals("MacBook Pro", products.getContent().get(0).getName());
    }

    @Test
    void givenNullFilters_whenFilteringProducts_thenReturnAllProductsInCategory() {
        pageRequest.setCategoryAlias("electronics");
        pageRequest.setFilter(null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(4, products.getTotalElements());
    }

    @Test
    void givenComplexSortingAndPaging_whenFilteringProducts_thenReturnCorrectlySortedAndPagedResults() {
        pageRequest.setCategoryAlias("electronics");
        pageRequest.setFilter(new String[]{"brand,Apple", "Processor,Intel i7", "RAM,16GB"});
        Pageable pageable = PageRequest.of(0, 1, PageUtil.generateSort("price", "asc"));

        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        assertFalse(products.isEmpty());
        assertEquals(1, products.getNumberOfElements());
        assertEquals(1, products.getTotalElements());
        assertEquals("MacBook Pro", products.getContent().get(0).getName());
        assertSortOrder(products.getContent(), Comparator.comparing(Product::getPrice));
    }
}
