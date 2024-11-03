package com.sellsphere.admin.product;

import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql"}, executionPhase =
        Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/clean_products.sql", "classpath:sql/clean_categories.sql", "classpath:sql/clean_brands.sql"}, executionPhase =
        Sql.ExecutionPhase.AFTER_TEST_CLASS)
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void givenProductWithDetailsAndImages_whenSaving_thenAllArePersisted() {
        // Given
        ProductTax productTax = new ProductTax();
        productTax.setId("1");

        Product product = new Product();
        product.setName("Test Product");
        product.setAlias("test_product");
        product.setShortDescription("Test short description");
        product.setFullDescription("Test full description");
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);
        product.setCost(new BigDecimal("500.00"));
        product.setPrice(new BigDecimal("600.00"));
        product.setDiscountPercent(new BigDecimal("10.00"));
        product.setLength(new BigDecimal("10.00"));
        product.setWidth(new BigDecimal("15.00"));
        product.setHeight(new BigDecimal("8.00"));
        product.setWeight(new BigDecimal("2.00"));
        product.setMainImage("test_image.jpg");
        product.setCategory(new Category(1));
        product.setBrand(new Brand(1));
        product.setTax(productTax);
        product.setContainsBatteryPi966(false);
        product.setContainsBatteryPi967(false);
        product.setContainsLiquids(false);
        product.setHsCode("12345678");

        ProductDetail detail = new ProductDetail();
        detail.setName("Test Detail");
        detail.setValue("Test Value");
        product.addProductDetail(detail);

        ProductImage image = new ProductImage();
        image.setName("test_image.jpg");
        product.addProductImage(image);

        // When
        Product savedProduct = productRepository.save(product);

        // Then
        assertNotNull(savedProduct.getId(), "Product ID should not be null.");
        assertEquals(1, savedProduct.getDetails().size(), "Product should have 1 detail.");
        assertEquals(1, savedProduct.getImages().size(), "Product should have 1 image.");
        assertNotNull(savedProduct.getCreatedTime(), "Created time should be set.");
    }

    @Test
    void givenProductWithoutDetailsOrImages_whenSaving_thenPersistSuccessfully() {
        // Given
        ProductTax productTax = new ProductTax();
        productTax.setId("2");

        Product product = new Product();
        product.setName("Test Product No Details");
        product.setAlias("test_product_no_details");
        product.setShortDescription("Short description");
        product.setFullDescription("Full description");
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);
        product.setCost(new BigDecimal("300.00"));
        product.setPrice(new BigDecimal("350.00"));
        product.setDiscountPercent(new BigDecimal("5.00"));
        product.setLength(new BigDecimal("10.00"));
        product.setWidth(new BigDecimal("12.00"));
        product.setHeight(new BigDecimal("8.00"));
        product.setWeight(new BigDecimal("1.50"));
        product.setMainImage("no_image.jpg");
        product.setCategory(new Category(2));
        product.setBrand(new Brand(2));
        product.setTax(productTax);
        product.setContainsBatteryPi966(false);
        product.setContainsBatteryPi967(false);
        product.setContainsLiquids(false);
        product.setHsCode("87654321");

        // When
        Product savedProduct = productRepository.save(product);

        // Then
        assertNotNull(savedProduct.getId(), "Product ID should not be null.");
        assertTrue(savedProduct.getDetails().isEmpty(), "Product should not have any details.");
        assertTrue(savedProduct.getImages().isEmpty(), "Product should not have any images.");
    }

    @Test
    void givenProduct_whenDeleting_thenDetailsAndImagesAreAlsoDeleted() {
        // Given
        Integer productId = 1;
        Optional<Product> productOptional = productRepository.findById(productId);
        assertTrue(productOptional.isPresent(), "Product with ID " + productId + " should exist.");
        Product product = productOptional.get();
        assertFalse(product.getDetails().isEmpty(), "Product should have details.");
        assertFalse(product.getImages().isEmpty(), "Product should have images.");

        List<ProductDetail> details = product.getDetails();
        List<ProductImage> images = product.getImages();

        // When
        productRepository.deleteById(productId);

        // Then
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertFalse(deletedProduct.isPresent(), "Product should be deleted.");

        for (ProductDetail detail : details) {
            assertNull(testEntityManager.find(ProductDetail.class, detail.getId()));
        }

        for (ProductImage image : images) {
            assertNull(testEntityManager.find(ProductImage.class, image.getId()));
        }
    }

    @Test
    void givenProductId_whenFindById_thenReturnProduct() {
        // Given
        Integer productId = 1;

        // When
        Optional<Product> productOptional = productRepository.findById(productId);

        // Then
        assertTrue(productOptional.isPresent(), "Product with ID " + productId + " should exist.");
    }

    @Test
    void givenProductName_whenFindByName_thenReturnProduct() {
        // When
        Optional<Product> productOptional = productRepository.findByName("Canon EOS 90D");

        // Then
        assertTrue(productOptional.isPresent(), "Product with name 'Canon EOS 90D' should exist.");
        assertEquals("Canon EOS 90D", productOptional.get().getName(),
                     "Product name should match."
        );
    }

    @Test
    void givenBrandId_whenFindAllByBrandId_thenReturnProducts() {
        // When
        List<Product> products = productRepository.findAllByBrandId(1);

        // Then
        assertFalse(products.isEmpty(), "Products for Brand ID 1 should be returned.");
    }

    @Test
    void givenCategoryId_whenFindAllByCategoryId_thenReturnProducts() {
        // When
        List<Product> products = productRepository.findAllByCategoryId(2);

        // Then
        assertFalse(products.isEmpty(), "Products for Category ID 2 should be returned.");
    }

    @Test
    void givenKeyword_whenSearchingProducts_thenReturnMatchingProducts() {
        // When
        Page<Product> products = productRepository.findAll("camera", PageRequest.of(0, 10));

        // Then
        assertFalse(products.isEmpty(), "Search results should not be empty.");
    }

    @Test
    void givenSortByPrice_whenFindingAll_thenReturnSortedProducts() {
        // When
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "price"));

        // Then
        assertFalse(products.isEmpty(), "Products should not be empty.");
        assertTrue(products.get(0).getPrice().compareTo(products.get(1).getPrice()) <= 0,
                   "Products should be sorted by price."
        );
    }

    @Test
    void givenProducts_whenPaging_thenReturnPagedResults() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 3, "price", Sort.Direction.ASC);

        // When
        Page<Product> result = productRepository.findAll(pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 3, 2, 5, "price", true);
    }

    @Test
    void givenKeywordAndPaging_whenSearching_thenReturnPagedResults() {
        // Given
        String keyword = "camera";
        Pageable pageable = PagingTestHelper.createPageRequest(0, 3, "name", Sort.Direction.ASC);

        // When
        Page<Product> result = productRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 3, 1, 3, "name", true);
    }

    @Test
    void givenEmptyKeywordAndPaging_whenSearching_thenReturnAllProducts() {
        // Given
        String keyword = "";
        Pageable pageable = PagingTestHelper.createPageRequest(0, 3, "name", Sort.Direction.ASC);

        // When
        Page<Product> result = productRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 3, 2, 5, "name", true);
    }

    @Test
    void givenSortByNameDesc_whenFindingAll_thenReturnSortedProductsInDescOrder() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 3, "name", Sort.Direction.DESC);

        // When
        Page<Product> result = productRepository.findAll(pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 3, 2, 5, "name", false);
    }

    @Test
    void givenPageOutOfBounds_whenPaging_thenReturnEmptyPage() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(5, 3, "price", Sort.Direction.ASC);

        // When
        Page<Product> result = productRepository.findAll(pageable);

        // Then
        assertTrue(result.getContent().isEmpty(), "Result should be an empty page.");
        assertEquals(0, result.getContent().size(), "Page content size should be 0.");
        assertEquals(2, result.getTotalPages(), "There should be 2 total pages.");
        assertEquals(5, result.getTotalElements(), "There should be 10 total products.");
    }
}
