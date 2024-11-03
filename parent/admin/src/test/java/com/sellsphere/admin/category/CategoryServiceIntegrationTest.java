package com.sellsphere.admin.category;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import com.sellsphere.common.entity.CategoryNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.S3TestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(S3MockExtension.class)
@Sql(scripts = {"classpath:sql/categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/clean_categories.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class CategoryServiceIntegrationTest {

    private static final String BUCKET_NAME = "my-demo-test-bucket";
    private static S3Client s3Client;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeAll
    static void setUpS3(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void givenExistingCategoryId_whenGetCategory_thenReturnCategory()
            throws CategoryNotFoundException {
        // Given
        Integer categoryId = 1;

        // When
        Category category = categoryService.getCategoryById(categoryId);

        // Then
        assertNotNull(category);
        assertEquals("Electronics", category.getName());
        assertEquals("electronics.png", category.getImage());
    }

    @Test
    void givenNonExistingCategoryId_whenGetCategory_thenThrowCategoryNotFoundException() {
        // Given
        Integer nonExistingCategoryId = 999;

        // When/Then
        assertThrows(CategoryNotFoundException.class,
                     () -> categoryService.getCategoryById(nonExistingCategoryId)
        );
    }

    @Test
    void whenListAllRootCategoriesSorted_thenReturnRootCategoriesWithCorrectHierarchy() {
        // When
        List<Category> categories = categoryService.listAllRootCategoriesSorted("name",
                                                                                Sort.Direction.ASC
        );

        // Then
        assertFalse(categories.isEmpty());

        // Assert hierarchy using helper method
        assertCategoryHierarchy(categories,
                                new String[]{"Electronics", "-Computers", "--Computer Components",
                                             "---CPU Processors Unit",
                                             "---Graphic Cards", "---Internal Hard Drives",
                                             "---Internal Optical Drives",
                                             "---Memory", "---Motherboard", "---Network Cards",
                                             "---Power Supplies",
                                             "---Solid State Drives", "---Sound Cards",
                                             "Vacuum cleaners"}
        );
    }

    private void assertCategoryHierarchy(List<Category> categories, String[] expectedNames) {
        // Assert the number of categories matches the expected number
        assertEquals(expectedNames.length, categories.size());

        // Assert the name of each category matches the expected hierarchy
        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], categories.get(i).getName(),
                         "Category name mismatch at index " + i
            );
        }
    }

    @Test
    void givenNewCategoryWithFile_whenSaveCategory_thenSaveFileInS3AndReturnCategory()
            throws IOException, CategoryIllegalStateException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-category.jpg",
                "image/jpeg",
                "Sample category image".getBytes()
        );

        Category newCategory = new Category();
        newCategory.setName("New Category");
        newCategory.setAlias("new_category");

        // Ensure `CategoryIcon` is not null and set a valid path to avoid DB constraint violations
        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("default-icon-path.png");
        newCategory.setCategoryIcon(categoryIcon);

        // When
        Category savedCategory = categoryService.saveCategory(newCategory, file);

        // Then
        assertNotNull(savedCategory.getId());
        assertEquals("test-category.jpg", savedCategory.getImage());

        // Verify the file is saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME,
                                      "category-photos/" + savedCategory.getId() + "/test" +
                                              "-category.jpg",
                                      file.getInputStream()
        );
    }

    @Test
    void givenNewCategoryWithoutFile_whenSaveCategory_thenReturnCategory()
            throws IOException, CategoryIllegalStateException {
        // Given
        Category newCategory = new Category();
        newCategory.setName("New Category");
        newCategory.setAlias("new_category");

        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("default-icon-path.png");
        newCategory.setCategoryIcon(categoryIcon);

        // When
        Category savedCategory = categoryService.saveCategory(newCategory, null);

        // Then
        assertNotNull(savedCategory.getId());
        assertNull(savedCategory.getImage());
    }

    @Test
    void givenPageNumber_whenListCategoriesByPage_thenReturnCategoriesForSpecificPage() {
        // Given
        int pageNum = 0;
        int expectedPageSize = 14;
        String sortField = "name";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "categoryList", sortField,
                                                                   Sort.Direction.ASC, null
        );

        // When
        categoryService.listCategoriesByPage(pageNum, helper);

        // Then
        List<Category> categories = helper.getContent();

        // Verify the paging results
        assertEquals(expectedPageSize, categories.size());

        // Verify the hierarchical order of categories (root and children)
        assertCategoryHierarchy(categories,
                                new String[]{"Electronics", "-Computers", "--Computer Components",
                                             "---CPU Processors Unit",
                                             "---Graphic Cards", "---Internal Hard Drives",
                                             "---Internal Optical Drives",
                                             "---Memory", "---Motherboard", "---Network Cards",
                                             "---Power Supplies",
                                             "---Solid State Drives", "---Sound Cards",
                                             "Vacuum cleaners"}
        );
    }

    @Test
    void givenKeyword_whenSearchCategories_thenReturnFilteredResults() {
        // Given
        int pageNum = 0;
        String keyword = "computer";
        String sortField = "name";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "categoryList", sortField,
                                                                   Sort.Direction.ASC, keyword
        );

        // When
        categoryService.listCategoriesByPage(pageNum, helper);

        // Then
        List<Category> categories = helper.getContent();
        assertFalse(categories.isEmpty());
        assertTrue(categories.stream().allMatch(
                category -> category.getName().toLowerCase().contains(
                        keyword) || category.getAlias().toLowerCase().contains(keyword)));
    }

    @Test
    void givenCategoryWithChildren_whenDeleteCategory_thenThrowCategoryIllegalStateException()
            throws CategoryNotFoundException {
        // Given
        Integer categoryId = 3; // Assuming this category has children

        // When/Then: Expect CategoryIllegalStateException when attempting to delete a category with children
        CategoryIllegalStateException exception = assertThrows(CategoryIllegalStateException.class,
                                                               () -> categoryService.deleteCategoryById(categoryId)
        );

        // Verify that the exception message contains information about child categories
        assertTrue(exception.getMessage().contains("Category has children"),
                   "Exception message should indicate the presence of child categories");
    }


    @Test
    void givenCategoryWithFile_whenSaveCategory_thenSaveFileAndCheckHierarchy()
            throws IOException, CategoryNotFoundException, CategoryIllegalStateException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hierarchy-category.jpg",
                "image/jpeg",
                "Sample hierarchy category image".getBytes()
        );

        Category newCategory = new Category();
        newCategory.setName("Hierarchy Category");
        newCategory.setAlias("hierarchy_category");

        // Set parent category (e.g., Computers category)
        Category parentCategory = categoryService.getCategoryById(2); // Computers category
        newCategory.setParent(parentCategory);

        // Ensure `CategoryIcon` is not null and set a valid path to avoid DB constraint violations
        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("default-icon-path.png");
        newCategory.setCategoryIcon(categoryIcon);

        // When
        Category savedCategory = categoryService.saveCategory(newCategory, file);

        // Then
        assertNotNull(savedCategory.getId());
        assertEquals("hierarchy-category.jpg", savedCategory.getImage());
        assertEquals("-1-2-", savedCategory.getAllParentIDs());

        // Verify the file is saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME,
                                      "category-photos/" + savedCategory.getId() + "/hierarchy" +
                                              "-category.jpg",
                                      file.getInputStream()
        );
    }

    @Test
    void givenCategoryId_whenToggleCategoryEnabledStatus_thenStatusOfCategoryAndChildrenIsUpdated()
            throws CategoryNotFoundException {
        // Given
        Integer parentId = 1; // Assuming 'Electronics' category is the parent
        boolean newStatus = false;

        // When
        categoryService.toggleCategoryEnabledStatus(parentId, newStatus);

        // Then
        Category parentCategory = categoryService.getCategoryById(parentId);
        assertFalse(parentCategory.isEnabled());

        // Check child categories' statuses
        Set<Category> childCategories = parentCategory.getChildren();
        assertFalse(childCategories.isEmpty());
        assertTrue(childCategories.stream().allMatch(child -> !child.isEnabled()));
    }

    @Test
    void givenNonExistingCategoryId_whenToggleCategoryEnabledStatus_thenThrowCategoryNotFoundException() {
        // Given
        Integer nonExistingCategoryId = 999;
        boolean newStatus = false;

        // When/Then
        assertThrows(CategoryNotFoundException.class,
                     () -> categoryService.toggleCategoryEnabledStatus(nonExistingCategoryId, newStatus));
    }

    @Test
    void givenCategoryId_whenDeleteCategoryBranch_thenCategoryAndChildrenAreDeleted()
            throws CategoryNotFoundException {
        // Given
        Integer parentId = 1; // 'Electronics' category with children
        Optional<Category> parent = categoryRepository.findById(parentId);
        assertTrue(parent.isPresent());
        assertFalse(parent.get().getChildren().isEmpty());

        Set<Category> childrens = parent.get().getChildren();

        // When
        categoryService.deleteCategoryBranch(parentId);

        // Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(parentId));

        // Verify that child categories are also deleted
        for (Category children : childrens) {
            assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(children.getId()));
        }
    }


}
