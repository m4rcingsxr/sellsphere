package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryService;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private Category electronics;
    private Category computers;
    private Category computerComponents;

    @BeforeEach
    void setUp() {
        electronics = new Category();
        electronics.setName("Electronics");
        electronics.setAlias("electronics");

        computers = new Category();
        computers.setName("Computers");
        computers.setAlias("computers");
        computers.setParent(electronics);

        computerComponents = new Category();
        computerComponents.setName("Computer Components");
        computerComponents.setAlias("computer_components");
        computerComponents.setParent(computers);
    }

    @Test
    void givenCategoryAlias_whenViewProductsByCategory_thenReturnProductsPageWithCategoryDetails() throws Exception {
        // Given
        String alias = "computers";
        List<Category> categoryParentList = List.of(electronics, computers);

        given(categoryService.getCategoryByAlias(alias)).willReturn(computers);
        given(categoryService.getCategoryParents(computers)).willReturn(categoryParentList);

        // When / Then
        mockMvc.perform(get("/c/{category_alias}", alias))
                .andExpect(status().isOk())
                .andExpect(view().name(ProductController.PRODUCTS_PATH))
                .andExpect(model().attributeExists("categoryParentList"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attribute("pageTitle", "Products by category: Computers"))
                .andExpect(model().attribute("category", computers));
    }

    @Test
    void givenKeyword_whenViewProductsByKeyword_thenReturnProductsPageWithKeyword() throws Exception {
        // Given
        String keyword = "laptop";

        // When / Then
        mockMvc.perform(get("/p/search/{keyword}", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name(ProductController.PRODUCTS_PATH))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attribute("keyword", keyword));
    }

    @Test
    void givenFilters_whenViewProductsByFilters_thenReturnProductsPage() throws Exception {
        // Given
        String[] filters = {"brand=Apple", "price=1000-2000"};

        // When / Then
        mockMvc.perform(get("/p/filters").param("filters", filters))
                .andExpect(status().isOk())
                .andExpect(view().name(ProductController.PRODUCTS_PATH));
    }
}
