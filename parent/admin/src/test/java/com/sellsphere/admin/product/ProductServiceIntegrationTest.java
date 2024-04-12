package com.sellsphere.admin.product;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.common.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import util.S3TestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql", "classpath:sql/categories.sql",
                "classpath:sql/brands_categories.sql", "classpath:sql/products.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ExtendWith(S3MockExtension.class)
class ProductServiceIntegrationTest {

    private static final String BUCKET_NAME = "my-demo-test-bucket";
    private static S3Client s3Client;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeAll
    static void setUpClient(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    @Transactional
    void givenProductAndFiles_whenSaving_thenShouldSaveFilesAndProduct() throws Exception {
        // Given
        MockMultipartFile newPrimaryImage = new MockMultipartFile(
                "file",
                "test-primary-image.jpg",
                "image/jpeg",
                "Primary image content".getBytes()
        );

        MockMultipartFile extraImage1 = new MockMultipartFile(
                "file",
                "test-extra-image1.jpg",
                "image/jpeg",
                "Extra image content 1".getBytes()
        );

        MockMultipartFile extraImage2 = new MockMultipartFile(
                "file",
                "test-extra-image2.jpg",
                "image/jpeg",
                "Extra image content 2".getBytes()
        );

        MultipartFile[] extraImages = {extraImage1, extraImage2};

        Product product = createProduct();

        // When
        Product savedProduct = productService.save(product, newPrimaryImage, extraImages);

        // Then
        assertNotNull(savedProduct.getId());
        assertEquals("test-primary-image.jpg", savedProduct.getMainImage());

        // Verify files are saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/test-primary-image.jpg", newPrimaryImage.getInputStream());
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/extras/test-extra-image1.jpg", extraImage1.getInputStream());
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/extras/test-extra-image2.jpg", extraImage2.getInputStream());

        // Verify product is saved in the repository
        Product fetchedProduct = entityManager.find(Product.class, savedProduct.getId());

        assertNotNull(fetchedProduct);
        assertEquals("test-primary-image.jpg", fetchedProduct.getMainImage());
    }

    @Test
    @Transactional
    void givenProductId_whenDeleteProduct_thenShouldRemoveProductAndFiles() throws Exception {
        // Given
        Product product = createProduct();
        productRepository.save(product);

        MockMultipartFile newPrimaryImage = new MockMultipartFile(
                "file",
                "test-primary-image.jpg",
                "image/jpeg",
                "Primary image content".getBytes()
        );

        MockMultipartFile extraImage1 = new MockMultipartFile(
                "file",
                "test-extra-image1.jpg",
                "image/jpeg",
                "Extra image content 1".getBytes()
        );

        MockMultipartFile extraImage2 = new MockMultipartFile(
                "file",
                "test-extra-image2.jpg",
                "image/jpeg",
                "Extra image content 2".getBytes()
        );

        MultipartFile[] extraImages = {extraImage1, extraImage2};

        Product savedProduct = productService.save(product, newPrimaryImage, extraImages);

        // Verify files are saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/test-primary-image.jpg", newPrimaryImage.getInputStream());
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/extras/test-extra-image1.jpg", extraImage1.getInputStream());
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/extras/test-extra-image2.jpg", extraImage2.getInputStream());

        // When
        productService.deleteProduct(savedProduct.getId());

        // Then
        assertFalse(productRepository.existsById(savedProduct.getId()), "Product should be deleted");

        // Verify that the files were deleted from S3
        List<String> keys = S3Utility.listFolder("product-photos/" + savedProduct.getId());
        assertTrue(keys.isEmpty(), "S3 folder should be empty after deletion");
    }

    public Product createProduct() {
        Product product = new Product();

        // Required fields
        product.setName("Sample Product");
        product.setAlias("sample_product");
        product.setShortDescription("This is a short description for the sample product.");
        product.setFullDescription("This is a detailed and long description for the sample product, providing all necessary information.");
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);
        product.setCost(new BigDecimal("100.00"));
        product.setPrice(new BigDecimal("150.00"));
        product.setDiscountPercent(new BigDecimal("10.00"));
        product.setLength(new BigDecimal("10.00"));
        product.setWidth(new BigDecimal("5.00"));
        product.setHeight(new BigDecimal("2.00"));
        product.setWeight(new BigDecimal("1.00"));
        product.setMainImage("sample_image.jpg");

        // Associated Category and Brand
        Category category = new Category();
        category.setId(1); // Assuming there's a category with ID 1
        product.setCategory(category);

        Brand brand = new Brand();
        brand.setId(1); // Assuming there's a brand with ID 1
        product.setBrand(brand);

        // Product details and images
        List<ProductDetail> details = new ArrayList<>();
        ProductDetail detail1 = new ProductDetail();
        detail1.setName("Detail 1");
        detail1.setValue("Value 1");
        detail1.setProduct(product);
        details.add(detail1);

        ProductDetail detail2 = new ProductDetail();
        detail2.setName("Detail 2");
        detail2.setValue("Value 2");
        detail2.setProduct(product);
        details.add(detail2);

        product.setDetails(details);

        List<ProductImage> images = new ArrayList<>();
        ProductImage image1 = new ProductImage();
        image1.setName("image1.jpg");
        image1.setProduct(product);
        images.add(image1);

        ProductImage image2 = new ProductImage();
        image2.setName("image2.jpg");
        image2.setProduct(product);
        images.add(image2);

        product.setImages(images);

        return product;
    }

    @Test
    @Transactional
    void givenExistingProductId_whenUpdateProductEnabledStatus_thenStatusIsUpdated() throws Exception {
        // Given
        Product product = createProduct();
        productRepository.save(product);

        // When
        productService.updateProductEnabledStatus(product.getId(), false);

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertFalse(updatedProduct.isEnabled());
    }

    @Test
    void givenNonExistingProductId_whenUpdateProductEnabledStatus_thenThrowProductNotFoundException() {
        // Given
        Integer nonExistingProductId = 999;
        boolean newStatus = false;

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateProductEnabledStatus(nonExistingProductId, newStatus);
        });
    }

}