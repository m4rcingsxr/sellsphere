package com.sellsphere.admin.category;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    private static final String DEFAULT_REDIRECT_URL = "/categories/page/0?sortField=name&sortDir=asc";

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenRootUrl_whenAccessed_thenRedirectToCategoryList() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));
    }

    @Test
    void givenNewCategoryUrl_whenAccessed_thenDisplayCreateCategoryForm() throws Exception {
        given(categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC)).willReturn(
                List.of());

        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/category_form"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attribute("pageTitle", "Create New Category"))
                .andExpect(model().attributeExists("categoryList"));
    }

    @Test
    void givenExistingCategoryId_whenEditCategoryUrlAccessed_thenDisplayEditCategoryForm() throws Exception {
        Category category = new Category();
        category.setId(1);
        category.setName("Test Category");
        List<Category> categories = List.of(new Category(1, "Electronics"));

        given(categoryService.getCategoryById(1)).willReturn(category);
        given(categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC)).willReturn(categories);

        mockMvc.perform(get("/categories/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("category/category_form"))
                .andExpect(model().attribute("category", category))
                .andExpect(model().attribute("pageTitle", "Edit Category [ID: 1]"))
                .andExpect(model().attributeExists("categoryList"))
                .andExpect(model().attribute("categoryList", categories));
    }

    @Test
    void givenValidCategoryAndFile_whenSaveCategory_thenRedirectToCategoryList() throws Exception {
        MockMultipartFile file = new MockMultipartFile("newImage", "test.png", MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        Category category = new Category();
        category.setId(1);
        category.setName("Test Category");
        category.setAlias("test");
        category.addCategoryIcon(new CategoryIcon(1, "icon"));

        willAnswer(invocation -> null).given(categoryService).saveCategory(any(Category.class), any());

        mockMvc.perform(multipart("/categories/save")
                                .file(file)
                                .flashAttr("category", category))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(categoryService).should().saveCategory(any(Category.class), any());
    }

    @Test
    void givenExistingCategoryId_whenDeleteCategory_thenRedirectToCategoryList() throws Exception {
        willDoNothing().given(categoryService).deleteCategoryById(1);

        mockMvc.perform(get("/categories/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(categoryService).should().deleteCategoryById(1);
    }

    @Test
    void givenExistingCategoryId_whenDeleteCategoryBranch_thenRedirectToCategoryList() throws Exception {
        willDoNothing().given(categoryService).deleteCategoryBranch(1);

        mockMvc.perform(get("/categories/delete_branch/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(categoryService).should().deleteCategoryBranch(1);
    }

    @Test
    void givenExistingCategoryId_whenUpdateEnabledStatus_thenRedirectToCategoryList() throws Exception {
        willDoNothing().given(categoryService).toggleCategoryEnabledStatus(1, true);

        mockMvc.perform(get("/categories/{id}/enabled/{status}", 1, true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(categoryService).should().toggleCategoryEnabledStatus(1, true);
    }

    @Test
    void givenValidPageNumber_whenListCategoriesByPage_thenReturnCategoriesForSpecificPage() throws Exception {
        int pageNum = 1;
        String sortField = "name";
        String sortDir = "asc";
        String keyword = "electronics";

        // Create test data
        List<Category> categories = List.of(
                new Category(1, "Electronics"),
                new Category(2, "Computers")
        );

        // Mock the service method to populate the helper
        doAnswer(invocation -> {
            Integer pageNumArg = invocation.getArgument(0);
            PagingAndSortingHelper helperArg = invocation.getArgument(1);

            // Simulate the helper updating the model
            helperArg.updateModelAttributes(pageNumArg, 1, 2L, categories);

            return null;
        }).when(categoryService).listCategoriesByPage(eq(pageNum), any(PagingAndSortingHelper.class));

        mockMvc.perform(get("/categories/page/{pageNum}", pageNum)
                                .param("sortField", sortField)
                                .param("sortDir", sortDir)
                                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("category/categories"))
                .andExpect(model().attributeExists("categoryList"))
                .andExpect(model().attribute("categoryList", categories));

        then(categoryService).should().listCategoriesByPage(eq(pageNum), any(PagingAndSortingHelper.class));
    }


    @Test
    void givenValidExportFormat_whenExportCategories_thenReturnFile() throws Exception {
        mockMvc.perform(get("/categories/export/csv"))
                .andExpect(status().isOk());

        then(categoryService).should().listAllRootCategoriesSorted("name", Sort.Direction.ASC);
    }
}
