package com.sellsphere.admin.product;

import com.sellsphere.admin.brand.BrandService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductTax;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    private static final String DEFAULT_REDIRECT_URL = "/products/page/0?sortField=name&sortDir=asc";

    @MockBean
    private ProductService productService;

    @MockBean
    private BrandService brandService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenRootUrl_whenAccessed_thenRedirectToProductList() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenNewProductUrl_whenAccessed_thenDisplayCreateProductForm() throws Exception {
        given(brandService.listAllBrands("name", Sort.Direction.ASC)).willReturn(List.of());

        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/product_form"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("pageTitle", "Create New Product"))
                .andExpect(model().attributeExists("brandList"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenExistingProductId_whenEditProductUrlAccessed_thenDisplayEditProductForm() throws Exception {
        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");
        Brand brand = new Brand(1);
        brand.setName("Canon");
        List<Brand> brands = List.of(brand);

        given(productService.getProductById(1)).willReturn(product);
        given(brandService.listAllBrands("name", Sort.Direction.ASC)).willReturn(brands);

        mockMvc.perform(get("/products/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("product/product_form"))
                .andExpect(model().attribute("product", product))
                .andExpect(model().attribute("pageTitle", "Edit Product [ID: 1]"))
                .andExpect(model().attributeExists("brandList"))
                .andExpect(model().attribute("brandList", brands));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenValidProductAndFiles_whenSaveProduct_thenRedirectToProductList() throws Exception {
        MockMultipartFile primaryImage = new MockMultipartFile("newImage", "test.png", MediaType.IMAGE_PNG_VALUE, "test image".getBytes());
        MockMultipartFile[] extraImages = {
                new MockMultipartFile("extraImages", "extra1.png", MediaType.IMAGE_PNG_VALUE, "extra image 1".getBytes()),
                new MockMultipartFile("extraImages", "extra2.png", MediaType.IMAGE_PNG_VALUE, "extra image 2".getBytes())
        };

        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");
        product.setAlias("test-product");
        product.setShortDescription("Short description of test product");
        product.setFullDescription("Full description of test product");
        product.setCost(new BigDecimal("100.00"));
        product.setPrice(new BigDecimal("150.00"));
        product.setDiscountPercent(new BigDecimal("10.00"));
        product.setLength(new BigDecimal("10.00"));
        product.setWidth(new BigDecimal("5.00"));
        product.setHeight(new BigDecimal("15.00"));
        product.setWeight(new BigDecimal("1.5"));

        // Set related entities (Category, Brand, Tax)
        Category category = new Category();
        category.setId(1);
        category.setName("Electronics");
        product.setCategory(category);

        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Test Brand");
        product.setBrand(brand);

        ProductTax tax = new ProductTax();
        tax.setId("1");
        tax.setName("Standard Tax");
        product.setTax(tax);

        product.setInStock(true);
        product.setEnabled(true);

        willAnswer(invocation -> null).given(productService).saveProduct(any(Product.class), any(), any());

        mockMvc.perform(multipart("/products/save")
                                .file(primaryImage)
                                .file(extraImages[0])
                                .file(extraImages[1])
                                .flashAttr("product", product))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(productService).should().saveProduct(any(Product.class), any(), any());
    }


    @Test
    void givenExistingProductId_whenDeleteProduct_thenRedirectToProductList() throws Exception {
        willDoNothing().given(productService).deleteProductById(1);

        mockMvc.perform(get("/products/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(productService).should().deleteProductById(1);
    }

    @Test
    void givenProductIdAndStatus_whenUpdateEnabledStatus_thenRedirectToProductList() throws Exception {
        willDoNothing().given(productService).updateProductEnabledStatus(1, true);

        mockMvc.perform(get("/products/{id}/enabled/{status}", 1, true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(productService).should().updateProductEnabledStatus(1, true);
    }

    @Test
    void givenValidExportFormat_whenExportProducts_thenReturnFile() throws Exception {
        mockMvc.perform(get("/products/export/csv"))
                .andExpect(status().isOk());

        then(productService).should().listAllProducts("id", Sort.Direction.ASC);
    }
}
