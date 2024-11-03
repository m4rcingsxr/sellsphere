package com.sellsphere.admin.product;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.PagingTestHelper;
import util.S3TestUtils;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(S3MockExtension.class)
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/products.sql", "classpath:sql/brands_categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/clean_products.sql", "classpath:sql/clean_categories.sql", "classpath:sql/clean_brands.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class ProductServiceIntegrationTest {

    private static final String BUCKET_NAME = "product-bucket";
    private static S3Client s3Client;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductService productService;

    @BeforeAll
    static void setUpS3(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void givenValidProductId_whenGetProduct_thenReturnProduct() throws ProductNotFoundException {
        // Given
        Integer productId = 1;

        // When
        Product product = productService.getProductById(productId);

        // Then
        assertNotNull(product);
        assertEquals("Canon EOS 90D", product.getName(), "Product name should be Canon EOS 90D");
        assertEquals(3, product.getDetails().size(), "Product should have 3 details.");
    }

    @Test
    void givenNonExistingProductId_whenGetProduct_thenThrowProductNotFoundException() {
        // Given
        Integer nonExistingProductId = 999;

        // When/Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(nonExistingProductId));
    }

    @Test
    void givenPageNumber_whenListProductsByPage_thenReturnProductsForSpecificPage() {
        // Given
        int pageNum = 0;
        String sortField = "name";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(), "productList", sortField, Sort.Direction.ASC, null);

        // When
        productService.paginateProducts(pageNum, helper);

        // Then
        PagingTestHelper.assertPagingResults(helper, 1, 5, 5, sortField, true);
    }

    @Test
    void givenNewProductWithFile_whenSaveProduct_thenSaveFileInS3AndReturnProduct() throws IOException {
        // Given
        MockMultipartFile primaryImage = new MockMultipartFile("file", "product-main.jpg", "image/jpeg", "Sample main image".getBytes());
        MockMultipartFile[] extraImages = {
                new MockMultipartFile("file", "extra1.jpg", "image/jpeg", "Sample extra image 1".getBytes()),
                new MockMultipartFile("file", "extra2.jpg", "image/jpeg", "Sample extra image 2".getBytes())
        };

        Product newProduct = new Product();
        newProduct.setName("TestProduct");
        newProduct.setAlias("test_product");
        newProduct.setShortDescription("Short description for test product");
        newProduct.setFullDescription("Long description for test product");
        newProduct.setCost(new BigDecimal("100.00"));
        newProduct.setPrice(new BigDecimal("150.00"));
        newProduct.setDiscountPercent(new BigDecimal("10.00"));
        newProduct.setLength(new BigDecimal("10.00"));
        newProduct.setWidth(new BigDecimal("5.00"));
        newProduct.setHeight(new BigDecimal("3.00"));
        newProduct.setWeight(new BigDecimal("2.00"));
        newProduct.setCategory(entityManager.find(Category.class, 1));
        newProduct.setBrand(entityManager.find(Brand.class, 1));
        newProduct.setTax(entityManager.find(ProductTax.class, 1));
        newProduct.setContainsBatteryPi966(false);
        newProduct.setContainsBatteryPi967(false);
        newProduct.setContainsLiquids(false);
        newProduct.setHsCode("12345678");

        // When
        Product savedProduct = productService.saveProduct(newProduct, primaryImage, extraImages
        );

        // Then
        assertNotNull(savedProduct.getId());
        assertEquals("product-main.jpg", savedProduct.getMainImage());

        // Verify the files are saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/product-main.jpg", primaryImage.getInputStream());
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/extras/extra1.jpg", extraImages[0].getInputStream());
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "product-photos/" + savedProduct.getId() + "/extras/extra2.jpg", extraImages[1].getInputStream());
    }

    @Test
    void givenProductWithoutFiles_whenSaveProduct_thenReturnProductWithoutImages() throws IOException {
        // Given
        Product newProduct = new Product();
        newProduct.setName("NoFileProduct");
        newProduct.setAlias("no_file_product");
        newProduct.setShortDescription("Short description for no file product");
        newProduct.setFullDescription("Long description for no file product");
        newProduct.setCost(new BigDecimal("100.00"));
        newProduct.setPrice(new BigDecimal("150.00"));
        newProduct.setDiscountPercent(new BigDecimal("10.00"));
        newProduct.setLength(new BigDecimal("10.00"));
        newProduct.setWidth(new BigDecimal("5.00"));
        newProduct.setHeight(new BigDecimal("3.00"));
        newProduct.setWeight(new BigDecimal("2.00"));
        newProduct.setCategory(entityManager.find(Category.class, 1));
        newProduct.setBrand(entityManager.find(Brand.class, 1));
        newProduct.setTax(entityManager.find(ProductTax.class, 1));
        newProduct.setContainsBatteryPi966(false);
        newProduct.setContainsBatteryPi967(false);
        newProduct.setContainsLiquids(false);
        newProduct.setHsCode("12345678");

        // When
        Product savedProduct = productService.saveProduct(newProduct, null, null
        );

        // Then
        assertNotNull(savedProduct.getId());
        assertNull(savedProduct.getMainImage());
        assertTrue(savedProduct.getImages().isEmpty(), "No extra images should be saved.");
    }

    @Test
    void givenProductName_whenCheckProductNameUnique_thenReturnCorrectResult() {
        // Given
        String existingName = "Canon EOS 90D";
        String uniqueName = "UniqueProduct";

        // When
        boolean isExistingNameUnique = productService.isProductNameUnique(null, existingName);
        boolean isUniqueNameUnique = productService.isProductNameUnique(null, uniqueName);

        // Then
        assertFalse(isExistingNameUnique);
        assertTrue(isUniqueNameUnique);
    }

    @Test
    void givenValidProductId_whenDeleteProduct_thenProductIsDeletedAndS3FilesRemoved() throws ProductNotFoundException {
        // Given
        Integer productId = 1;

        // When
        productService.deleteProductById(productId);

        // Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));

        // Verify that S3 folder is removed
        assertTrue(S3Utility.listFolder("product-photos/" + productId).isEmpty(), "Product images should be removed from S3.");
    }

    @Test
    void givenProductWithDetails_whenSaveProduct_thenDetailsArePersisted() throws IOException {
        // Given
        Product newProduct = new Product();
        newProduct.setName("DetailedProduct");
        newProduct.setAlias("detailed_product");
        newProduct.setShortDescription("Short description for detailed product");
        newProduct.setFullDescription("Long description for detailed product");
        newProduct.setCost(new BigDecimal("100.00"));
        newProduct.setPrice(new BigDecimal("150.00"));
        newProduct.setDiscountPercent(new BigDecimal("10.00"));
        newProduct.setLength(new BigDecimal("10.00"));
        newProduct.setWidth(new BigDecimal("5.00"));
        newProduct.setHeight(new BigDecimal("3.00"));
        newProduct.setWeight(new BigDecimal("2.00"));
        newProduct.setCategory(entityManager.find(Category.class, 1));
        newProduct.setBrand(entityManager.find(Brand.class, 1));
        newProduct.setTax(entityManager.find(ProductTax.class, 1));
        newProduct.setContainsBatteryPi966(false);
        newProduct.setContainsBatteryPi967(false);
        newProduct.setContainsLiquids(false);
        newProduct.setHsCode("12345678");

        String[] detailNames = {"Color", "Weight"};
        String[] detailValues = {"Red", "1.2kg"};

        // When
        Product savedProduct = productService.saveProduct(newProduct, null, null
        );
        ProductHelper.addProductDetails(savedProduct, detailNames, detailValues);

        // Then
        assertNotNull(savedProduct.getId());
        assertFalse(savedProduct.getDetails().isEmpty());
        assertEquals("Color", savedProduct.getDetails().get(0).getName());
        assertEquals("Red", savedProduct.getDetails().get(0).getValue());
    }

    @Test
    void givenProductWithoutDetails_whenSaveProduct_thenNoDetailsArePersisted() throws IOException {
        // Given
        Product newProduct = new Product();
        newProduct.setName("NoDetailProduct");
        newProduct.setAlias("no_detail_product");
        newProduct.setShortDescription("Short description for no detail product");
        newProduct.setFullDescription("Long description for no detail product");
        newProduct.setCost(new BigDecimal("100.00"));
        newProduct.setPrice(new BigDecimal("150.00"));
        newProduct.setDiscountPercent(new BigDecimal("10.00"));
        newProduct.setLength(new BigDecimal("10.00"));
        newProduct.setWidth(new BigDecimal("5.00"));
        newProduct.setHeight(new BigDecimal("3.00"));
        newProduct.setWeight(new BigDecimal("2.00"));
        newProduct.setCategory(entityManager.find(Category.class, 1));
        newProduct.setBrand(entityManager.find(Brand.class, 1));
        newProduct.setTax(entityManager.find(ProductTax.class, 1));
        newProduct.setContainsBatteryPi966(false);
        newProduct.setContainsBatteryPi967(false);
        newProduct.setContainsLiquids(false);
        newProduct.setHsCode("12345678");

        // When
        Product savedProduct = productService.saveProduct(newProduct, null, null
        );

        // Then
        assertNotNull(savedProduct.getId());
        assertTrue(savedProduct.getDetails().isEmpty(), "No product details should be saved.");
    }
}
