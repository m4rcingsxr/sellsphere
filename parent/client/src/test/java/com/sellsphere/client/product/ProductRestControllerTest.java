package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1);
        category.setName("Laptops");
        category.setAlias("laptops");

        given(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).willReturn(
                Optional.of(category));

        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");

        Product product1 = new Product();
        product1.setId(1);
        product1.setName("Product One");
        product1.setCategory(category);
        product1.setBrand(brand);
        Product product2 = new Product();
        product2.setId(2);
        product2.setName("Product Two");
        product2.setCategory(category);
        product2.setBrand(brand);
        ProductPageResponse mockResponse = ProductPageResponse.builder().page(0).pageSize(
                10).totalElements(2).totalPages(1).content(
                List.of(new BasicProductDto(product1), new BasicProductDto(product2))).build();

        given(productService.listProductsPage(any(ProductPageRequest.class))).willReturn(
                mockResponse);

        Map<String, Map<String, Long>> mockFilterCounts = Map.of("Color", Map.of("Red", 2L), "Size",
                                                                 Map.of("Medium", 2L), "Material",
                                                                 Map.of("Plastic", 1L)
        );

        given(productService.getAvailableFilterCounts(any(ProductPageRequest.class))).willReturn(
                mockFilterCounts);
    }

    @ParameterizedTest
    @CsvSource({"'brand,Apple|Color,Red'"})
    void givenFilters_whenGetProducts_thenShouldReturnPageOfProducts(String filter)
            throws Exception {
        // Execute the request and verify the response
        mockMvc.perform(
                get("/filter/products").param("filter", filter.split("\\|")).param("category_alias",
                                                                                   "laptops"
                )).andExpect(status().isOk()).andExpect(
                jsonPath("$.totalElements", is(2))).andExpect(
                jsonPath("$.content[0].name", is("Product One"))).andExpect(
                jsonPath("$.content[1].name", is("Product Two")));
    }

    @ParameterizedTest
    @CsvSource({"'brand,Apple|Color,Red'"})
    void givenFilters_whenGetFilterCounts_thenShouldReturnFilterCounts(String filter)
            throws Exception {
        // Execute the request and verify the response
        mockMvc.perform(
                get("/filter/counts").param("filter", filter.split("\\|")).param("category_alias",
                                                                                 "laptops"
                )).andExpect(status().isOk()).andExpect(jsonPath("$.Color.Red", is(2))).andExpect(
                jsonPath("$.Size.Medium", is(2))).andExpect(jsonPath("$.Material.Plastic", is(1)));
    }
}
