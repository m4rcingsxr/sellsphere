package com.sellsphere.admin.brand;

import com.sellsphere.common.entity.Constants;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BrandControllerIntegrationTest {

    private static final String BRAND_FORM_PATH = "brand/brand_form";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFirstPage_ShouldRedirectToDefaultUrl() throws Exception {
        mockMvc.perform(get("/brands"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(BrandController.DEFAULT_REDIRECT_URL.replace("redirect:", "")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPageWithoutKeyword_ShouldReturnCorrectPage() throws Exception {
        int testPageNum = 0;
        int expectedSize = 10;

        mockMvc.perform(get("/brands/page/{pageNum}", testPageNum)
                                .param("sortField", "name")
                                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("brandList", "totalPages", "totalItems", "sortDir", "sortField"))
                .andExpect(model().attributeDoesNotExist("keyword"))
                .andExpect(model().attribute("brandList", hasSize(expectedSize)))
                .andExpect(view().name("brand/brands"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showBrandForm_WhenNewBrand_ShouldDisplayFormWithDefaultBrand() throws Exception {
        mockMvc.perform(get("/brands/new"))
                .andExpect(status().isOk())
                .andExpect(view().name(BRAND_FORM_PATH))
                .andExpect(model().attributeExists("brand", "categoryList", "pageTitle"))
                .andExpect(model().attribute("pageTitle", "Create New Brand"))
                .andExpect(model().attribute("brand", hasProperty("name", isEmptyOrNullString())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showBrandForm_WhenExistingBrand_ShouldDisplayFormWithBrandData() throws Exception {
        int existingBrandId = 1;
        mockMvc.perform(get("/brands/edit/{id}", existingBrandId))
                .andExpect(status().isOk())
                .andExpect(view().name(BRAND_FORM_PATH))
                .andExpect(model().attributeExists("brand", "categoryList", "pageTitle"))
                .andExpect(model().attribute("pageTitle", containsString("Edit Brand")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveBrand_WhenInvalidData_ShouldReturnFormWithErrors() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("newImage", "", "image/jpeg", new byte[]{});

        // When & Then
        mockMvc.perform(multipart("/brands/save")
                                .file(file)
                                .param("name", "") // Invalid name (empty)
                                .param("logo", "") // Invalid logo (empty)
                                .with(csrf())

                )
                .andExpect(status().isOk())
                .andExpect(view().name(BRAND_FORM_PATH))
                .andExpect(model().attributeHasFieldErrors("brand", "name", "logo"))
                .andExpect(model().attributeExists("categoryList", "pageTitle"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveBrand_WhenValidData_ShouldRedirectToDefaultUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("newImage", "logo.png", "image/png", "test image".getBytes());

        // When & Then
        mockMvc.perform(multipart("/brands/save")
                                .file(file)
                                .param("name", "Valid Brand Name")
                                .param("logo", "logo.png")
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(BrandController.DEFAULT_REDIRECT_URL.replace("redirect:", "")))
                .andExpect(flash().attributeExists(Constants.SUCCESS_MESSAGE));
    }

}
