package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class ProductRestControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidRequest_whenPageFilteredProducts_thenReturnsProductPageResponse() throws Exception {
        Product product = createTestProduct();
        BasicProductDto productDto = new BasicProductDto(product);

        ProductPageResponse productPageResponse = ProductPageResponse.builder()
                .page(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .content(Collections.singletonList(productDto))
                .build();

        when(productService.listProductsPage(any(ProductPageRequest.class))).thenReturn(productPageResponse);

        mockMvc.perform(get("/filter/products")
                                .param("filter", "")
                                .param("category_alias", "laptops")
                                .param("pageNum", "0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryName").value("Laptops"))
                .andExpect(jsonPath("$.content[0].brandName").value("Test Brand"));
    }

    @Test
    void givenInvalidRequest_whenPageFilteredProducts_thenReturnsBadRequest() throws Exception {

        mockMvc.perform(get("/filter/products")
                                .param("filter", "")
                                .param("category_alias", "")
                                .param("pageNum", "0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenMissingPageNum_whenPageFilteredProducts_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/filter/products")
                                .param("filter", "")
                                .param("category_alias", "laptops")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenKeywordInsteadOfCategory_whenPageFilteredProducts_thenReturnsProductPageResponse() throws Exception {
        Product product = createTestProduct();
        BasicProductDto productDto = new BasicProductDto(product);

        ProductPageResponse productPageResponse = ProductPageResponse.builder()
                .page(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .content(Collections.singletonList(productDto))
                .build();

        when(productService.listProductsPage(any(ProductPageRequest.class))).thenReturn(productPageResponse);

        mockMvc.perform(get("/filter/products")
                                .param("filter", "")
                                .param("keyword", "Laptop")
                                .param("pageNum", "0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryName").value("Laptops"))
                .andExpect(jsonPath("$.content[0].brandName").value("Test Brand"));
    }

    @Test
     void givenProductPageRequest_whenListFilterCounts_thenReturnFilterCounts() throws Exception {
        Map<String, Map<String, Long>> filterCountMap = new HashMap<>();
        Map<String, Long> colorCounts = new HashMap<>();
        colorCounts.put("Red", 2L);
        colorCounts.put("Blue", 1L);
        Map<String, Long> sizeCounts = new HashMap<>();
        sizeCounts.put("M", 2L);
        sizeCounts.put("L", 1L);

        filterCountMap.put("Color", colorCounts);
        filterCountMap.put("Size", sizeCounts);

        when(productService.getFilterCounts(any(ProductPageRequest.class)))
                .thenReturn(filterCountMap);

        mockMvc.perform(get("/filter/counts")
                                .param("pageNum", "0")
                                .param("filter", "A,B")
                                .param("category_alias", "laptops")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Color.Red").value(2))
                .andExpect(jsonPath("$.Color.Blue").value(1))
                .andExpect(jsonPath("$.Size.M").value(2))
                .andExpect(jsonPath("$.Size.L").value(1));
    }

    private Product createTestProduct() {
        Brand brand = new Brand();
        brand.setName("Test Brand");

        Category category = new Category();
        category.setName("Laptops");
        category.setAlias("laptops");

        Product product = new Product();
        product.setBrand(brand);
        product.setCategory(category);
        product.setShortDescription("Short description");
        product.setFullDescription("Full description");

        return product;
    }
}
