package com.sellsphere.admin.category;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {
        "classpath:sql/categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private static final String EXPECTED_DEFAULT_REDIRECT_URL = "/categories/page/0?sortField" +
            "=name&sortDir=asc";

    private static final String EXPECTED_FORM_PATH = "category/category_form";

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFirstPage_ShouldRedirectToDefaultUrl() throws Exception {
        mockMvc.perform(get("/categories")).andExpect(status().is3xxRedirection()).andExpect(
                redirectedUrl(EXPECTED_DEFAULT_REDIRECT_URL));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPageWithKeyword_ShouldReturnCorrectPage() throws Exception {
        int testPageNum = 0;
        int expectedSize = 1;

        mockMvc.perform(
                get("/categories/page/{pageNum}", testPageNum).param("sortField", "name").param(
                        "sortDir", "asc").param("keyword", "electronics")).andExpect(
                status().isOk()).andExpect(
                model().attributeExists("categoryList", "totalPages", "totalItems", "sortField",
                                        "sortDir", "keyword"
                )).andExpect(model().attribute("categoryList", hasSize(expectedSize))).andExpect(
                model().attribute("totalItems", is(1L))).andExpect(
                model().attribute("totalPages", is(1))).andExpect(
                view().name("category/categories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showCategoryForm_WhenNewCategory_ShouldDisplayForm() throws Exception {
        mockMvc.perform(
                get("/categories/new").with(csrf())).andExpect(
                status().isOk()).andExpect(view().name(EXPECTED_FORM_PATH)).andExpect(
                model().attributeExists("category")).andExpect(
                model().attribute("pageTitle", "Create new category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showCategoryForm_WhenExistingCategory_ShouldDisplayFormWithCategoryDetails()
            throws Exception {
        int existingCategoryId = 1;  // Assuming there's a category with ID 1
        mockMvc.perform(get("/categories/edit/{id}", existingCategoryId)).andExpect(
                status().isOk()).andExpect(view().name(EXPECTED_FORM_PATH)).andExpect(
                model().attributeExists("category")).andExpect(model().attribute("pageTitle",
                                                                                 containsString(
                                                                                         "Edit Category [ID: " + existingCategoryId + "]")
        ));
    }

}