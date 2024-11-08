package com.sellsphere.client.product;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @MockBean
    private ProductService productService;

    @Test
    void givenProductIds_whenGetProductsByIds_thenReturnProductList() throws Exception {
        // Given
        Category category = new Category();
        category.setId(1);
        Brand brand = new Brand();
        brand.setId(1);

        Product product1 = new Product();
        product1.setId(1);
        product1.setName("Product1");
        product1.setPrice(BigDecimal.TEN);
        product1.setCategory(category);
        product1.setBrand(brand);

        Product product2 = new Product();
        product2.setId(2);
        product2.setPrice(BigDecimal.TEN);
        product2.setName("Product2");
        product2.setCategory(category);
        product2.setBrand(brand);

        List<Product> products = List.of(product1, product2);
        when(productService.getProductsByIds(anyList())).thenReturn(products);

        // When & Then
        mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[1, 2]"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product2"));
    }
}