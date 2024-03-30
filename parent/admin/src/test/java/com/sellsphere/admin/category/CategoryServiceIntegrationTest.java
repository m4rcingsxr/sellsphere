package com.sellsphere.admin.category;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
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
    void givenCategoryAndFile_whenSaving_thenShouldSaveFileAndCategory() throws Exception {

        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Sample image content".getBytes()
        );
        Category category = generateComputersCategory();

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
}
