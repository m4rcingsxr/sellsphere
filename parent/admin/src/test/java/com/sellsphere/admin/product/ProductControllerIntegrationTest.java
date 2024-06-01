package com.sellsphere.admin.product;

import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql", "classpath:sql/categories.sql",
                "classpath:sql/brands_categories.sql", "classpath:sql/products.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ProductControllerIntegrationTest {

    private static final String EXPECTED_REDIRECT_URL = "/products/page/0?sortField=name&sortDir=asc";
    private static final String PRODUCT_FORM = "product/product_form";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;



    @Test
    @WithMockUser(roles = "ADMIN")
    void listFirstPage_ShouldRedirectToDefaultUrl() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPageWithoutKeyword_ShouldReturnCorrectPage() throws Exception {
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 10));

        int testPageNum = 0;

        mockMvc.perform(get("/products/page/{pageNum}", testPageNum)
                                .param("sortField", "name")
                                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("productList", "totalPages", "totalItems", "sortDir", "sortField"))
                .andExpect(model().attributeDoesNotExist("keyword"))
                .andExpect(view().name("product/products"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showProductForm_WhenNewProduct_ShouldDisplayFormWithDefaultProduct() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name(PRODUCT_FORM))
                .andExpect(model().attributeExists("product", "brandList", "pageTitle"))
                .andExpect(model().attribute("pageTitle", "Create New Product"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showProductForm_WhenExistingProduct_ShouldDisplayFormWithProductData() throws Exception {
        int productId = 1;
        Product product = new Product();
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        mockMvc.perform(get("/products/edit/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(view().name(PRODUCT_FORM))
                .andExpect(model().attributeExists("product", "brandList", "pageTitle"))
                .andExpect(model().attribute("pageTitle", containsString("Edit Product")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_ShouldSaveProductAndRedirect() throws Exception {
        // Mock file uploads
        MockMultipartFile newPrimaryImage = new MockMultipartFile(
                "newImage", "test-primary-image.jpg", "image/jpeg", "Primary image content".getBytes()
        );
        MockMultipartFile extraImage1 = new MockMultipartFile(
                "extraImages", "test-extra-image1.jpg", "image/jpeg", "Extra image content 1".getBytes()
        );
        MockMultipartFile extraImage2 = new MockMultipartFile(
                "extraImages", "test-extra-image2.jpg", "image/jpeg", "Extra image content 2".getBytes()
        );

        // Create a product entity for the test
        Product product = new Product();
        product.setId(1);
        product.setName("com.sellsphere.admin.Test Product");
        product.setAlias("test_product");
        product.setShortDescription("Short description");
        product.setFullDescription("Full description");
        product.setCost(new BigDecimal("100.00"));
        product.setPrice(new BigDecimal("150.00"));
        product.setDiscountPercent(new BigDecimal("10.00"));
        product.setLength(new BigDecimal("10.00"));
        product.setWidth(new BigDecimal("5.00"));
        product.setHeight(new BigDecimal("2.00"));
        product.setWeight(new BigDecimal("1.00"));
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Define parameters to be passed
        String[] values = {"Detail1", "Detail2"};
        String[] names = {"name1", "name2"};

        // Mock static methods in ProductHelper
        try (MockedStatic<ProductHelper> mockedProductHelper = mockStatic(ProductHelper.class)) {
            mockedProductHelper.when(() -> ProductHelper.addProductDetails(any(Product.class), any(String[].class), any(String[].class)))
                    .then(invocation -> null);
            mockedProductHelper.when(() -> ProductHelper.addProductImages(any(Product.class), any(MultipartFile[].class)))
                    .then(invocation -> null);
            mockedProductHelper.when(() -> ProductHelper.saveExtraImages(any(Product.class), any(MultipartFile[].class)))
                    .then(invocation -> null);
            mockedProductHelper.when(() -> ProductHelper.savePrimaryImage(any(Product.class), any(MultipartFile.class)))
                    .then(invocation -> null);

            // Perform the save request
            mockMvc.perform(multipart("/products/save")
                                    .file(newPrimaryImage)
                                    .file(extraImage1)
                                    .file(extraImage2)
                                    .with(csrf())
                                    .param("name", "com.sellsphere.admin.Test Product")
                                    .param("alias", "test_product")
                                    .param("shortDescription", "Short description")
                                    .param("fullDescription", "Full description")
                                    .param("cost", "100.00")
                                    .param("price", "150.00")
                                    .param("discountPercent", "10.00")
                                    .param("length", "10.00")
                                    .param("width", "5.00")
                                    .param("height", "2.00")
                                    .param("weight", "1.00")
                                    .param("enabled", "true")
                                    .param("inStock", "true")
                                    .param("brand", "1")
                                    .param("category", "1")
                                    .param("values", values)
                                    .param("names", names))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));

            // Verify the static methods were called
            mockedProductHelper.verify(() -> ProductHelper.addProductDetails(any(Product.class), eq(values), eq(names)), times(1));
            mockedProductHelper.verify(() -> ProductHelper.addProductImages(any(Product.class), any(MultipartFile[].class)), times(1));
            mockedProductHelper.verify(() -> ProductHelper.saveExtraImages(any(Product.class), any(MultipartFile[].class)), times(1));
            mockedProductHelper.verify(() -> ProductHelper.savePrimaryImage(any(Product.class), any(MultipartFile.class)), times(1));
        }
    }

}
