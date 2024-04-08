package com.sellsphere.admin.product;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql", "classpath:sql/categories.sql",
                "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql"}, executionPhase =
        Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ProductControllerIntegrationTest {

    private static final String EXPECTED_REDIRECT_URL = "/products/page/0?sortField=name" +
            "&sortDir=asc";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFirstPage_ShouldRedirectToDefaultUrl() throws Exception {
        mockMvc.perform(get("/products")).andExpect(status().is3xxRedirection()).andExpect(
                redirectedUrl(EXPECTED_REDIRECT_URL));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPageWithoutKeyword_ShouldReturnCorrectPage() throws Exception {
        int testPageNum = 0;
        int expectedSize = 10;

        mockMvc.perform(get("/products/page/{pageNum}", testPageNum).param("sortField", "name").param(
                "sortDir", "asc")).andExpect(status().isOk()).andExpect(
                model().attributeExists("productList", "totalPages", "totalItems", "sortDir",
                                        "sortField"
                )).andExpect(model().attributeDoesNotExist("keyword")).andExpect(
                model().attribute("productList", hasSize(expectedSize))).andExpect(
                view().name("product/products"));
    }

}