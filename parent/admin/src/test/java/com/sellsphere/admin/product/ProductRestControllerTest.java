package com.sellsphere.admin.product;

import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.easyship.payload.HsCode;
import com.sellsphere.easyship.payload.HsCodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductRestControllerTest {

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductTaxRepository productTaxRepository;

    @MockBean
    private EasyshipService easyshipService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidIdAndName_whenCheckProductNameUniqueness_thenReturnTrue() throws Exception {
        given(productService.isProductNameUnique(anyInt(), anyString())).willReturn(true);

        mockMvc.perform(post("/products/check_uniqueness")
                                .param("id", "1")
                                .param("name", "UniqueProduct")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(productService).should().isProductNameUnique(1, "UniqueProduct");
    }

    @Test
    void givenNameWithoutId_whenCheckProductNameUniqueness_thenReturnTrue() throws Exception {
        given(productService.isProductNameUnique(null, "UniqueProduct")).willReturn(true);

        mockMvc.perform(post("/products/check_uniqueness")
                                .param("name", "UniqueProduct")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(productService).should().isProductNameUnique(null, "UniqueProduct");
    }

    @Test
    void givenNonUniqueName_whenCheckProductNameUniqueness_thenReturnFalse() throws Exception {
        given(productService.isProductNameUnique(anyInt(), anyString())).willReturn(false);

        mockMvc.perform(post("/products/check_uniqueness")
                                .param("id", "1")
                                .param("name", "ExistingProduct")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(productService).should().isProductNameUnique(1, "ExistingProduct");
    }

    @Test
    void givenValidTaxType_whenFindByTaxType_thenReturnProductTaxes() throws Exception {
        List<ProductTax> taxes = List.of(new ProductTax("1", "Standard Tax", TaxType.DIGITAL, "desc"));

        given(productTaxRepository.findByType(TaxType.DIGITAL)).willReturn(taxes);

        mockMvc.perform(get("/products/tax/DIGITAL")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Standard Tax"));

        then(productTaxRepository).should().findByType(TaxType.DIGITAL);
    }

    @Test
    void givenValidPageAndCode_whenListHsCodes_thenReturnHsCodeResponse() throws Exception {
        HsCode hsCode1 = HsCode.builder().code("1234").description("Electronics").build();
        HsCode hsCode2 = HsCode.builder().code("5678").description("Toys").build();
        List<HsCode> hsCodes = List.of(hsCode1, hsCode2);

        HsCodeResponse hsCodeResponse = HsCodeResponse.builder()
                .hsCodes(hsCodes)
                .build();

        given(easyshipService.fetchHsCodes(1, "1234", null)).willReturn(hsCodeResponse);

        mockMvc.perform(get("/products/hs-codes")
                                .param("page", "1")
                                .param("code", "1234")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hsCodes[0].code").value("1234"))
                .andExpect(jsonPath("$.hsCodes[0].description").value("Electronics"))
                .andExpect(jsonPath("$.hsCodes[1].code").value("5678"))
                .andExpect(jsonPath("$.hsCodes[1].description").value("Toys"));

        then(easyshipService).should().fetchHsCodes(1, "1234", null);
    }

    @Test
    void givenValidBrandId_whenListProductsByBrand_thenReturnProductList() throws Exception {
        // Mocking Product entities with Category and Brand set
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("Category1");

        Brand brand1 = new Brand();
        brand1.setId(1);
        brand1.setName("Brand1");

        Product product1 = new Product(1);
        product1.setName("Product1");
        product1.setAlias("alias1");
        product1.setCategory(category1);
        product1.setBrand(brand1);
        product1.setInStock(true);
        product1.setPrice(new BigDecimal("100"));
        product1.setDiscountPercent(new BigDecimal("10"));

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Category2");

        Brand brand2 = new Brand();
        brand2.setId(2);
        brand2.setName("Brand2");

        Product product2 = new Product(2);
        product2.setName("Product2");
        product2.setAlias("alias2");
        product2.setCategory(category2);
        product2.setBrand(brand2);
        product2.setInStock(true);
        product2.setPrice(new BigDecimal("150"));
        product2.setDiscountPercent(new BigDecimal("15"));

        // Mock the service to return the products when listAllProductsByBrand is called
        given(productService.listAllProductsByBrand(1)).willReturn(List.of(product1, product2));

        // Perform the request and assert the response
        mockMvc.perform(get("/products/brand/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[0].categoryName").value("Category1"))
                .andExpect(jsonPath("$[0].brandName").value("Brand1"))
                .andExpect(jsonPath("$[1].name").value("Product2"))
                .andExpect(jsonPath("$[1].categoryName").value("Category2"))
                .andExpect(jsonPath("$[1].brandName").value("Brand2"));

        // Verify that the service method was called
        then(productService).should().listAllProductsByBrand(1);
    }


    @Test
    void givenValidCategoryId_whenListProductsByCategory_thenReturnProductList() throws Exception {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("Category1");

        Brand brand1 = new Brand();
        brand1.setId(1);
        brand1.setName("Brand1");

        Product product1 = new Product(1);
        product1.setName("Product1");
        product1.setAlias("alias1");
        product1.setCategory(category1);
        product1.setBrand(brand1);
        product1.setInStock(true);
        product1.setPrice(new BigDecimal("100"));
        product1.setDiscountPercent(new BigDecimal("10"));

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Category2");

        Brand brand2 = new Brand();
        brand2.setId(2);
        brand2.setName("Brand2");

        Product product2 = new Product(2);
        product2.setName("Product2");
        product2.setAlias("alias2");
        product2.setCategory(category2);
        product2.setBrand(brand2);
        product2.setInStock(true);
        product2.setPrice(new BigDecimal("150"));
        product2.setDiscountPercent(new BigDecimal("15"));

        // Return the list of products when service method is called
        given(productService.listAllProductsByCategory(1)).willReturn(List.of(product1, product2));

        // Perform the request and assert the results
        mockMvc.perform(get("/products/category/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[0].categoryName").value("Category1"))
                .andExpect(jsonPath("$[0].brandName").value("Brand1"))
                .andExpect(jsonPath("$[1].name").value("Product2"))
                .andExpect(jsonPath("$[1].categoryName").value("Category2"))
                .andExpect(jsonPath("$[1].brandName").value("Brand2"));

        then(productService).should().listAllProductsByCategory(1);
    }


    @Test
    void givenValidKeyword_whenListProductsByKeyword_thenReturnProductList() throws Exception {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("Category1");

        Brand brand1 = new Brand();
        brand1.setId(1);
        brand1.setName("Brand1");

        Product product1 = new Product(1);
        product1.setName("Product1");
        product1.setCategory(category1);
        product1.setBrand(brand1);
        product1.setInStock(true);
        product1.setPrice(new BigDecimal("100"));
        product1.setDiscountPercent(new BigDecimal("10"));

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Category2");

        Brand brand2 = new Brand();
        brand2.setId(2);
        brand2.setName("Brand2");

        Product product2 = new Product(2);
        product2.setName("Product2");
        product2.setCategory(category2);
        product2.setBrand(brand2);
        product2.setInStock(true);
        product2.setPrice(new BigDecimal("150"));
        product2.setDiscountPercent(new BigDecimal("15"));

        // Return the list of products when service method is called
        given(productService.searchProductsByKeyword("electronics")).willReturn(List.of(product1, product2));

        // Perform the request and assert the results
        mockMvc.perform(get("/products/search/{keyword}", "electronics")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[0].categoryName").value("Category1"))
                .andExpect(jsonPath("$[0].brandName").value("Brand1"))
                .andExpect(jsonPath("$[1].name").value("Product2"))
                .andExpect(jsonPath("$[1].categoryName").value("Category2"))
                .andExpect(jsonPath("$[1].brandName").value("Brand2"));

        then(productService).should().searchProductsByKeyword("electronics");
    }
}
