package com.sellsphere.admin.product;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)

@Sql(scripts = {"classpath:sql/brands.sql", "classpath:sql/categories.sql",
                "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql"}, executionPhase =
        Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class RestProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenProductName_whenCheckUniqueness_thenReturnResult() throws Exception {
        // Given
        Integer productId = 1;
        String productName = "Test Product";

        when(productService.isNameUnique(productId, productName)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/products/check_uniqueness")
                                .param("id", String.valueOf(productId))
                                .param("name", productName)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

}