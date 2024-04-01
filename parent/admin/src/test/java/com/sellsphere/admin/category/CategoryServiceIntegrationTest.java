package com.sellsphere.admin.category;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import com.sellsphere.common.entity.CategoryNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.S3TestUtils;

import java.util.List;

import static com.sellsphere.admin.category.TestCategoryHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {
        "classpath:sql/categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ExtendWith(S3MockExtension.class)
class CategoryServiceIntegrationTest {

    private static final String BUCKET_NAME = "my-demo-test-bucket";
    private static S3Client s3Client;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeAll
    static void setUpClient(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

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

    @Test
    @Transactional
    void givenCategoryAndFile_whenSaving_thenShouldSaveFileAndCategory() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Sample image content".getBytes()
        );
        Category category = generateComputersCategoryWithoutId(); // Generate category without manually setting ID

        // When
        Category savedCategory = categoryService.save(category, file);

        // Then
        assertNotNull(savedCategory.getId());
        assertEquals("test-image.jpg", savedCategory.getImage());

        // Verify file is saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "category-photos/" + savedCategory.getId() + "/test-image.jpg", file.getInputStream());

        // Verify category is saved in the repository
        Category fetchedCategory = entityManager.find(Category.class, savedCategory.getId());

        assertNotNull(fetchedCategory);
        assertEquals("test-image.jpg", fetchedCategory.getImage());
    }

    @Test
    void whenNameNotExists_thenReturnTrue() {
        // When
        boolean result = categoryService.isNameUnique(null, "uniqueName");

        // Then
        assertTrue(result);
    }

    @Test
    void whenNameExistsWithSameId_thenReturnTrue() {
        // Given
        Integer existingCategoryId = 1; // ID of the "Electronics" category

        // When
        boolean result = categoryService.isNameUnique(existingCategoryId, "Electronics");

        // Then
        assertTrue(result);
    }


    @Test
    void whenNameExistsWithDifferentId_thenReturnFalse() {
        // Given
        Integer differentId = 2; // ID of the "Electronics" category

        // When
        boolean result = categoryService.isNameUnique(differentId, "Electronics"); // Different ID but same name

        // Then
        assertFalse(result);
    }

    @Test
    void whenAliasNotExists_thenReturnTrue() {
        // When
        boolean result = categoryService.isAliasUnique(null, "uniqueAlias");

        // Then
        assertTrue(result);
    }

    @Test
    void whenAliasExistsWithSameId_thenReturnTrue() {
        // Given
        Integer existingCategoryId = 1; // ID of the "Electronics" category

        // When
        boolean result = categoryService.isAliasUnique(existingCategoryId, "electronics");

        // Then
        assertTrue(result);
    }

    @Test
    void whenAliasExistsWithDifferentId_thenReturnFalse() {
        // Given
        Integer differentId = 2;

        // When
        boolean result = categoryService.isAliasUnique(differentId, "electronics"); // Different ID but same alias

        // Then
        assertFalse(result);
    }

    @Test
    void givenCategoryWithNoChildren_whenDelete_thenCategoryDeleted() throws CategoryNotFoundException,
            CategoryIllegalStateException {
        // Given
        Category categoryWithNoChildren = categoryRepository.findByAlias("vacuum_cleaners").orElseThrow(CategoryNotFoundException::new);

        // When
        categoryService.delete(categoryWithNoChildren.getId());

        // Then
        assertFalse(categoryRepository.existsById(categoryWithNoChildren.getId()), "Category should be deleted");
    }

    @Test
    void givenCategoryWithChildren_whenDelete_thenThrowException()
            throws CategoryNotFoundException {
        // Given
        Category categoryWithChildren = categoryRepository.findByAlias("computers").orElseThrow(CategoryNotFoundException::new);

        // When & Then
        assertThrows(CategoryIllegalStateException.class, () -> categoryService.delete(categoryWithChildren.getId()));
    }

    @Test
    void givenNonExistentCategory_whenDelete_thenThrowException() {
        // Given
        int nonExistentCategoryId = 999;

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(nonExistentCategoryId));
    }

    @Test
    void givenCategoryWithChildren_whenDeleteCategoryBranch_thenCategoryAndChildrenDeleted() throws CategoryNotFoundException {
        // Given
        Category categoryWithChildren = categoryRepository.findByAlias("computers").orElseThrow(CategoryNotFoundException::new);

        // When
        categoryService.deleteCategoryBranch(categoryWithChildren.getId());

        // Then
        TestCategoryHelper.assertCategoryBranchDeleted(categoryRepository, categoryWithChildren);
    }

    @Test
    void givenNonExistentCategory_whenDeleteCategoryBranch_thenThrowException() {
        // Given
        int nonExistentCategoryId = 999;

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategoryBranch(nonExistentCategoryId));
    }

    @Test
    void givenCategory_whenToggleEnabledStatus_thenStatusToggled() throws CategoryNotFoundException {
        // Given
        Category category = categoryRepository.findByAlias("computers").orElseThrow(CategoryNotFoundException::new);
        boolean initialStatus = category.isEnabled();

        // When
        categoryService.toggleCategoryEnabledStatus(category.getId(), !initialStatus);

        // Then
        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow(CategoryNotFoundException::new);
        assertEquals(!initialStatus, updatedCategory.isEnabled(), "Category enabled status should be toggled");
        TestCategoryHelper.assertCategoryEnabledStatus(updatedCategory, !initialStatus);
    }

    @Test
    void givenNonExistentCategory_whenToggleEnabledStatus_thenThrowException() {
        // Given
        int nonExistentCategoryId = 999;

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.toggleCategoryEnabledStatus(nonExistentCategoryId, true));
    }


}
