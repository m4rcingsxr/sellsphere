package com.sellsphere.admin.brand;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@AutoConfigureMockMvc
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BrandRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void isAliasUnique_WithValidRequest_ShouldReturnTrue() throws Exception {

        mockMvc.perform(post("/brands/check_uniqueness")
                                .with(csrf())
                                .param("name", "UniqueBrandName")
                                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void isAliasUnique_WithExistingNameAndDifferentId_ShouldReturnFalse() throws Exception {
        mockMvc.perform(post("/brands/check_uniqueness")
                                .with(csrf())
                                .param("name", "Canon")
                                .param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void isAliasUnique_WithNameTooLong_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/brands/check_uniqueness")
                                .with(csrf())
                                .param("name", "ThisBrandNameIsTooLongToBeValidAndShouldTriggerValidation")
                                .param("id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Brand name length should not exceeds 45 characters")))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }

}
