package com.sellsphere.admin.category;

import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CategoryRestControllerTest {

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidAliasAndId_whenCheckAliasUniqueness_thenReturnTrue() throws Exception {
        given(categoryService.isCategoryAliasUnique(anyInt(), anyString())).willReturn(true);

        mockMvc.perform(post("/categories/check-alias-uniqueness")
                                .param("id", "1")
                                .param("alias", "UniqueAlias")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(categoryService).should().isCategoryAliasUnique(1, "UniqueAlias");
    }

    @Test
    void givenValidAliasWithoutId_whenCheckAliasUniqueness_thenReturnTrue() throws Exception {
        given(categoryService.isCategoryAliasUnique(null, "UniqueAlias")).willReturn(true);

        mockMvc.perform(post("/categories/check-alias-uniqueness")
                                .param("alias", "UniqueAlias")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(categoryService).should().isCategoryAliasUnique(null, "UniqueAlias");
    }

    @Test
    void givenNonUniqueAlias_whenCheckAliasUniqueness_thenReturnFalse() throws Exception {
        given(categoryService.isCategoryAliasUnique(anyInt(), anyString())).willReturn(false);

        mockMvc.perform(post("/categories/check-alias-uniqueness")
                                .param("id", "1")
                                .param("alias", "ExistingAlias")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(categoryService).should().isCategoryAliasUnique(1, "ExistingAlias");
    }

    @Test
    void givenInvalidAliasLength_whenCheckAliasUniqueness_thenThrowIllegalArgumentException() throws Exception {
        mockMvc.perform(post("/categories/check-alias-uniqueness")
                                .param("alias", "a".repeat(65)) // Exceeds the 64 character limit
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidNameAndId_whenCheckNameUniqueness_thenReturnTrue() throws Exception {
        given(categoryService.isCategoryNameUnique(anyInt(), anyString())).willReturn(true);

        mockMvc.perform(post("/categories/check-name-uniqueness")
                                .param("id", "1")
                                .param("name", "UniqueName")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(categoryService).should().isCategoryNameUnique(1, "UniqueName");
    }

    @Test
    void givenValidNameWithoutId_whenCheckNameUniqueness_thenReturnTrue() throws Exception {
        given(categoryService.isCategoryNameUnique(null, "UniqueName")).willReturn(true);

        mockMvc.perform(post("/categories/check-name-uniqueness")
                                .param("name", "UniqueName")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(categoryService).should().isCategoryNameUnique(null, "UniqueName");
    }

    @Test
    void givenNonUniqueName_whenCheckNameUniqueness_thenReturnFalse() throws Exception {
        given(categoryService.isCategoryNameUnique(anyInt(), anyString())).willReturn(false);

        mockMvc.perform(post("/categories/check-name-uniqueness")
                                .param("id", "1")
                                .param("name", "ExistingName")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(categoryService).should().isCategoryNameUnique(1, "ExistingName");
    }

    @Test
    void givenInvalidNameLength_whenCheckNameUniqueness_thenThrowIllegalArgumentException() throws Exception {
        mockMvc.perform(post("/categories/check-name-uniqueness")
                                .param("name", "a".repeat(129)) // Exceeds the 128 character limit
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenFetchAllCategories_thenReturnCategoryHierarchy() throws Exception {
        Category rootCategory = new Category();
        rootCategory.setName("Electronics");
        List<Category> categories = List.of(rootCategory);

        given(categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC)).willReturn(categories);
        given(categoryService.createHierarchy(anyList())).willReturn(categories);

        mockMvc.perform(get("/categories/fetch-all")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        then(categoryService).should().listAllRootCategoriesSorted("name", Sort.Direction.ASC);
        then(categoryService).should().createHierarchy(anyList());
    }
}
