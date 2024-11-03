package com.sellsphere.admin.category;

import com.sellsphere.admin.brand.BrandRepository;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryIconRepository categoryIconRepository;
    @Autowired
    private BrandRepository brandRepository;

    @Test
    void givenCategoryWithIcon_whenSaving_thenIconIsSaved() {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        category.setAlias("test_category");
        category.setImage("test_category.png");
        category.setEnabled(true);

        CategoryIcon icon = new CategoryIcon();
        icon.setIconPath("<i>test_icon</i>");
        category.addCategoryIcon(icon);

        // When
        Category savedCategory = categoryRepository.save(category);

        // Then
        assertNotNull(savedCategory.getId(), "Category ID should not be null after saving.");
        assertNotNull(savedCategory.getCategoryIcon(), "CategoryIcon should be saved.");
        assertEquals("<i>test_icon</i>", savedCategory.getCategoryIcon().getIconPath(),
                     "CategoryIcon path should match the saved value."
        );
    }


    @Test
    void givenCategoryWithIcon_whenCascadeDeleting_thenIconIsDeleted() {
        // Given
        Optional<Category> categoryOptional = categoryRepository.findById(1);
        assertTrue(categoryOptional.isPresent(), "Category with ID 1 should exist.");
        Category category = categoryOptional.get();
        CategoryIcon icon = category.getCategoryIcon();
        assertNotNull(icon, "CategoryIcon should be present for the category.");

        // When
        categoryRepository.delete(category);

        // Then
        Optional<CategoryIcon> deletedIcon = categoryIconRepository.findById(1);
        assertFalse(deletedIcon.isPresent(), "CategoryIcon should be deleted with the category.");
    }


    @Test
    void givenCategoryWithChildren_whenSaving_thenChildrenAreSaved() {
        // Given
        Category parent = categoryRepository.findById(1).orElseThrow();
        assertEquals(1, parent.getChildren().size());

        Category child = new Category();
        child.setName("Test Child Category");
        child.setAlias("test_child_category");
        child.setImage("image.jpg");
        child.setParent(parent);
        parent.addChild(child);

        // When
        categoryRepository.save(parent);

        // Then
        Category savedParent = categoryRepository.findById(1).orElseThrow();
        assertEquals(2, savedParent.getChildren().size(), "Parent should have 2 children.");
    }

    @Test
    void givenCategoryWithChildren_whenCascadeDeleting_thenChildrenAreDeleted() {
        // Given
        Category parent = categoryRepository.findById(2).orElseThrow();
        assertFalse(parent.getChildren().isEmpty(), "Parent category should have children.");

        // When
        categoryRepository.delete(parent);

        // Then
        assertFalse(categoryRepository.findById(2).isPresent(), "Parent category should be deleted.");
        assertTrue(categoryRepository.findAllByParentIsNull(
                           Sort.unsorted()).stream().noneMatch(c -> c.getParent() != null && c.getParent().getId().equals(2)),
                   "All children of the parent category should also be deleted."
        );
    }

    @Test
    void givenRootCategories_whenListingAll_thenReturnSortedCategories() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "name", Sort.Direction.ASC);

        // When
        Page<Category> result = categoryRepository.findAllByParentIsNull(pageable);

        // Then (only root categories)
        PagingTestHelper.assertPagingResults(result, 2, 1, 2, "name", true);
        assertEquals("Electronics", result.getContent().get(0).getName(), "First root category should be 'Electronics'.");
    }

    @Test
    void givenCategoryName_whenFindByName_thenReturnExistingCategory() {
        // When
        Optional<Category> categoryOptional = categoryRepository.findByName("Electronics");

        // Then
        assertTrue(categoryOptional.isPresent(), "Category with the name 'Electronics' should exist.");
    }

    @Test
    void givenCategoryWithChildren_whenDeleting_thenThrowException() {
        // Given
        Category parent = categoryRepository.findById(1).orElseThrow();
        assertFalse(parent.getChildren().isEmpty(), "Category should have children.");

        // When
        categoryRepository.delete(parent);

        // Then
        Optional<Category> child = categoryRepository.findById(2);
        assertTrue(child.isEmpty());
    }

    @Test
    void givenCategory_whenUpdatingEnabledStatus_thenStatusIsUpdated() {
        // Given
        Category parent = categoryRepository.findById(1).orElseThrow();

        // When
        parent.setEnabled(false);
        categoryRepository.save(parent);

        // Then
        Category updatedParent = categoryRepository.findById(1).orElseThrow();
        assertFalse(updatedParent.isEnabled(), "Parent category should be disabled.");
        assertTrue(updatedParent.getChildren().stream().allMatch(Category::isEnabled),
                   "All child categories should inherit the enabled status from the parent."
        );
    }

    @Test
    void givenCategoryWithBrands_whenCategoryDeleted_thenBrandsCategoryAssociationsAreCleared() {
        // Given
        Integer categoryId = 1; // Electronics category
        Optional<Category> category = categoryRepository.findById(categoryId);
        assertTrue(category.isPresent(), "Category with ID " + categoryId + " should exist.");
        assertFalse(category.get().getBrands().isEmpty(), "Category should be associated with brands.");

        // When
        categoryRepository.deleteById(categoryId);

        // Then
        Optional<Category> deletedCategory = categoryRepository.findById(categoryId);
        assertFalse(deletedCategory.isPresent(), "Category with ID " + categoryId + " should be deleted.");

        // Verify that the brands are no longer associated with the deleted category
        for (Brand brand : brandRepository.findAll()) {
            assertFalse(brand.getCategories().contains(category.get()),
                        "Brand should no longer be associated with the deleted category.");
        }
    }

}