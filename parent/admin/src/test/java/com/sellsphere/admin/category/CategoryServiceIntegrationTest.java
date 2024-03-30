package com.sellsphere.admin.category;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static com.sellsphere.admin.category.TestCategoryHelper.assertHierarchy;
import static com.sellsphere.admin.category.TestCategoryHelper.assertRootCategoriesSortedByName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {
        "classpath:sql/categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void whenListAllRootCategoriesSorted_thenSortedCategoriesInHierarchicalOrderReturned() {

        // When
        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", "asc");

        // Then
        assertRootCategoriesSortedByName(categoryList);
        categoryList.forEach(category -> assertHierarchy(category, categoryList));
    }


    @Test
    void givenKeyword_whenPageCategories_thenUseSearchRepository() {
        // Given
        Integer pageNum = 0;
        String keyword = "computers";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "categories", "name", "asc",
                                                                   keyword
        );

        // When
        categoryService.listPage(pageNum, helper);

        // Then
        ModelAndViewContainer model = helper.getModel();
        List<Category> categories = (List<Category>) model.getModel().get("categories");
        assertFalse(categories.isEmpty(), "Categories should be found");
        categories.forEach(
                category -> assertTrue(category.getName().toLowerCase().contains(keyword),
                                       "Category name should contain keyword"
                ));
    }

    @Test
    void givenNoKeyword_whenListingCategories_thenListCategoriesInHierarchicalOrderReturned() {
        // Given
        List<Category> rootCategories = List.of(entityManager.find(Category.class, 1),
                                                entityManager.find(Category.class, 14)
        );
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "categories", "name", "asc", null
        );

        Integer pageNum = 0;

        // When
        categoryService.listPage(pageNum, helper);

        // Then
        ModelAndViewContainer model = helper.getModel();
        List<Category> categories = (List<Category>) model.getModel().get("categories");

        assertRootCategoriesSortedByName(categories);
        categories.forEach(category -> assertHierarchy(category, rootCategories));
    }


}
