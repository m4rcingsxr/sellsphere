package com.sellsphere.admin.product;

import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import com.sellsphere.common.entity.ProductImage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql", "classpath:sql/categories.sql",
                "classpath:sql/brands_categories.sql", "classpath:sql/products.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void givenProduct_WhenSave_ThenProductIsPersisted() {
        // Given
        Product product = new Product();
        product.setName("Test Product");
        product.setAlias("testProduct");
        product.setShortDescription("Test Short Description");
        product.setFullDescription("Test Full Description");
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);
        product.setCost(BigDecimal.valueOf(100));
        product.setPrice(BigDecimal.valueOf(150));
        product.setDiscountPercent(BigDecimal.valueOf(10));
        product.setLength(BigDecimal.valueOf(10));
        product.setWidth(BigDecimal.valueOf(5));
        product.setHeight(BigDecimal.valueOf(2));
        product.setWeight(BigDecimal.valueOf(1));
        product.setMainImage("testProduct.png");
        product.setCategory(new Category(2));
        product.setBrand(new Brand(1));

        // _When
        Product savedProduct = productRepository.save(product);

        // _Then
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());
        assertEquals("Test Product", savedProduct.getName());
    }

    @Test
    void givenExistingProduct_WhenFindById_ThenProductIsFound() {
        // Given
        Integer productId = 1; // Assume this product ID exists in the database

        // _When
        Optional<Product> optionalProduct = productRepository.findById(productId);

        // _Then
        assertTrue(optionalProduct.isPresent());
        Product product = optionalProduct.get();
        assertEquals(productId, product.getId());
    }

    @Test
    void givenNonExistingProduct_WhenFindById_ThenProductIsNotFound() {
        // Given
        Integer productId = 999; // Assume this product ID does not exist in the database

        // _When
        Optional<Product> optionalProduct = productRepository.findById(productId);

        // _Then
        assertFalse(optionalProduct.isPresent());
    }

    @Test
    void givenExistingProduct_WhenDelete_ThenProductIsRemoved() {
        // Given
        Integer productId = 1; // Assume this product ID exists in the database

        // _When
        productRepository.deleteById(productId);
        Optional<Product> optionalProduct = productRepository.findById(productId);

        // _Then
        assertFalse(optionalProduct.isPresent());
    }

    @ParameterizedTest
    @CsvSource({
            "name,ASC",
            "price,DESC"
    })
    void givenSortingParams_WhenFindAll_ThenProductsAreSorted(String sortField, String sortDirection) {
        // Given
        int page = 0;
        int size = 5;
        PageRequest pageRequest = PagingTestHelper.createPageRequest(page, size, sortField, sortDirection);

        // _When
        Page<Product> productPage = productRepository.findAll(pageRequest);

        // _Then
        PagingTestHelper.assertPagingResults(productPage, size, 2, 10, sortField, sortDirection.equalsIgnoreCase("ASC"));
    }

    @Test
    void givenExistingProduct_WhenUpdate_ThenProductIsUpdatedAndUpdateTimeIsSet() {
        // Given
        Integer productId = 1; // Assume this product ID exists in the database
        Optional<Product> optionalProduct = productRepository.findById(productId);
        assertTrue(optionalProduct.isPresent());
        Product product = optionalProduct.get();

        // Update product details
        product.setName("Updated Product");
        product.setPrice(BigDecimal.valueOf(200));
        product.updateProductTimestamp();

        // _When
        Product updatedProduct = productRepository.save(product);

        // _Then
        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(BigDecimal.valueOf(200), updatedProduct.getPrice());
        assertNotNull(updatedProduct.getProductUpdate());
        assertNotNull(updatedProduct.getProductUpdate().getUpdatedTime());
    }

    @Test
    void givenProductDetailsAndImages_WhenCascadePersist_ThenEntitiesArePersisted() {
        // Given
        Product product = new Product();
        product.setName("Test Product");
        product.setAlias("testProduct");
        product.setShortDescription("Test Short Description");
        product.setFullDescription("Test Full Description");
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);
        product.setCost(BigDecimal.valueOf(100));
        product.setPrice(BigDecimal.valueOf(150));
        product.setDiscountPercent(BigDecimal.valueOf(10));
        product.setLength(BigDecimal.valueOf(10));
        product.setWidth(BigDecimal.valueOf(5));
        product.setHeight(BigDecimal.valueOf(2));
        product.setWeight(BigDecimal.valueOf(1));
        product.setMainImage("testProduct.png");
        product.setCategory(new Category(2));
        product.setBrand(new Brand(1));

        ProductDetail detail = new ProductDetail();
        detail.setName("Detail Name");
        detail.setValue("Detail Value");

        ProductImage image = new ProductImage();
        image.setName("Image Name");

        product.addProductDetail(detail);
        product.addProductImage(image);

        // _When
        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        // _Then
        Product foundProduct = entityManager.find(Product.class, savedProduct.getId());
        assertNotNull(foundProduct);
        assertEquals(1, foundProduct.getDetails().size());
        assertEquals(1, foundProduct.getImages().size());
    }

    @Test
    void givenExistingProductWithDetailsAndImages_WhenRemoveDetailsAndImages_ThenOrphanRemovalDeletesThem() {
        // Given
        Integer productId = 1; // Assume this product ID exists in the database
        Optional<Product> optionalProduct = productRepository.findById(productId);
        assertTrue(optionalProduct.isPresent());
        Product product = optionalProduct.get();

        // _When
        product.getDetails().clear();
        product.getImages().clear();
        productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        // _Then
        Product foundProduct = entityManager.find(Product.class, productId);
        assertNotNull(foundProduct);
        assertTrue(foundProduct.getDetails().isEmpty());
        assertTrue(foundProduct.getImages().isEmpty());
    }
}
