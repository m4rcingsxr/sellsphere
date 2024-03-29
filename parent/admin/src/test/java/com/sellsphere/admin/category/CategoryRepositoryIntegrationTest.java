package com.sellsphere.admin.category;

import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {
        "classpath:sql/categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryIconRepository categoryIconRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void whenSaveCategory_thenCategoryIsSaved() {
        // Given
        Category networkCards = entityManager.find(Category.class, 13);

        Category newCategory = new Category();
        newCategory.setName("New Category");
        newCategory.setAlias("new_category");
        newCategory.setParent(networkCards);
        newCategory.setImage("category.img");

        // When
        Category savedCategory = categoryRepository.save(newCategory);

        // Then
        assertNotNull(savedCategory.getId(), "Saved category should have a non-null ID");
        assertEquals("New Category", savedCategory.getName(), "Saved category name should match");
        assertEquals("new_category", savedCategory.getAlias(), "Saved category alias should match");
        assertEquals(networkCards, savedCategory.getParent(), "Saved category parent should match with expected parent");
    }

    @Test
    void whenSaveRootCategoryWithoutIcon_thenConstraintViolationExceptionIsThrown() {
        // Given
        Category newCategory = new Category();
        newCategory.setName("New Category");
        newCategory.setAlias("new_category");
        newCategory.setImage("category.img");

        // When
        // Then
        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(newCategory));
    }

    @Test
    void whenSaveCategoryWithIcon_thenCategoryAndIconIsPersisted() {
        // Given
        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("<i>category_icon</i>");

        Category rootCategory = new Category();
        rootCategory.setName("New Category");
        rootCategory.setAlias("new_category");
        rootCategory.setImage("category.img");
        rootCategory.setCategoryIcon(categoryIcon);

        // When
        Category savedRootCategory = categoryRepository.save(rootCategory);

        // Then
        assertNotNull(savedRootCategory.getId(), "Root category should be persisted");
        assertNotNull(savedRootCategory.getCategoryIcon(), "Category icon should be persisted");
        assertEquals("New Category", savedRootCategory.getName(), "Category name should match");
        assertEquals("new_category", savedRootCategory.getAlias(), "Category alias should match");
    }

    @Test
    void whenSaveCategoryWithChildren_thenCategoryAndChildrenArePersisted() { // cascade persist
        // Given
        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("<i>category_icon</i>");

        Category parentCategory = new Category();
        parentCategory.setName("Parent Category");
        parentCategory.setAlias("parent_category");
        parentCategory.setImage("parent_image.png");
        parentCategory.setCategoryIcon(categoryIcon);

        Category childCategory = new Category();
        childCategory.setName("Child Category");
        childCategory.setAlias("child_category");
        childCategory.setImage("child_image.png");

        parentCategory.addChild(childCategory);

        // When
        Category savedParentCategory = categoryRepository.save(parentCategory);

        // Then
        assertNotNull(savedParentCategory.getId(), "Saved parent category should have a non-null ID");
        assertNotNull(savedParentCategory.getChildren(), "Saved parent category should have children");
        assertFalse(savedParentCategory.getChildren().isEmpty(), "Saved parent category should have at least one child");
        Category savedChildCategory = savedParentCategory.getChildren().iterator().next();
        assertNotNull(savedChildCategory.getId(), "Saved child category should have a non-null ID");
        assertEquals(savedParentCategory, savedChildCategory.getParent(), "Parent of saved child category should be the saved parent category");
    }

    @Test
    void whenChildrenIsUpdated_thenCategoryAndChildrenAreUpdated() { // cascade merge
        // Given
        String expectedName = "newCategory";
        Category parentCategory = entityManager.find(Category.class, 1);

        // When: Update child category
        Category child = parentCategory.getChildren().iterator().next();
        child.setName(expectedName);
        categoryRepository.save(parentCategory);

        // Then: Fetch updated category and verify the changes
        Category updatedParentCategory = categoryRepository.findById(parentCategory.getId()).orElseThrow();
        Category updatedChildCategory = updatedParentCategory.getChildren().iterator().next();
        assertEquals(expectedName, updatedChildCategory.getName(), "Child category name should be updated");
    }

    @Test
    void whenDeleteRootCategory_thenCategoryChildrenAndRelatedIconIsRemoved() { // cascade delete
        // Given
        int rootId = 1;

        // When
        categoryRepository.deleteById(rootId);

        // Then
        assertFalse(categoryRepository.existsById(rootId), "Root category should be deleted");
        long expectedCount = 0;
        assertEquals(expectedCount, categoryRepository.count(), "Expected count should be " + expectedCount);
        assertEquals(expectedCount, categoryIconRepository.count(), "Expected count should be " + expectedCount);
    }

    @Test
    void whenRemoveCategoryFromChildren_thenCategoryChildrenIsRemoved() {  // orphan removal
        // Given
        Category parentCategory = entityManager.find(Category.class, 1);
        Category childCategory = entityManager.find(Category.class, 2);

        // When
        parentCategory.removeChild(childCategory);
        categoryRepository.saveAndFlush(parentCategory);
        entityManager.clear(); // Clear the persistence context to ensure entities are fetched from the database

        // Then
        Optional<Category> removedChildCategory = categoryRepository.findById(childCategory.getId());
        assertTrue(removedChildCategory.isEmpty(), "Child category should be removed from the database");
    }

    @Test
    void whenDeleteById_thenCategoryIsDeleted() {
        Integer categoryId = 13;

        // When
        categoryRepository.deleteById(categoryId);

        // Then
        Optional<Category> deletedCategory = categoryRepository.findById(categoryId);
        assertTrue(deletedCategory.isEmpty(), "Category should not be found after deletion");
    }

    @Test
    void whenExistsById_thenReturnTrue() {
        // Given
        int categoryId = 8;

        // When
        boolean exists = categoryRepository.existsById(categoryId);

        // Then
        assertTrue(exists, "Category should exist");
    }

    @Test
    void whenCount_thenShouldReturnExpectedCount() {
        // Given
        long expectedCount = 13;

        // When
        long count = categoryRepository.count();

        // Then
        assertEquals(expectedCount, count, "Category count should match with expectedCount");
    }

}