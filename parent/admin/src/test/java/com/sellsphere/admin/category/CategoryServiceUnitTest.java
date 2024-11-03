package com.sellsphere.admin.category;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import com.sellsphere.common.entity.CategoryNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MultipartFile file;

    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void givenValidCategoryId_whenGetCategoryById_thenVerifyRepositoryCalled() throws CategoryNotFoundException {
        // Given
        Integer categoryId = 1;
        Category category = new Category();
        category.setId(categoryId);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // When
        categoryService.getCategoryById(categoryId);

        // Then
        then(categoryRepository).should().findById(categoryId);
    }

    @Test
    void givenNonExistingCategoryId_whenGetCategoryById_thenThrowCategoryNotFoundExceptionAndVerifyRepositoryCalled() {
        // Given
        Integer categoryId = 999;

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(categoryId));
        then(categoryRepository).should().findById(categoryId);
    }

    @Test
    void givenValidSortOptions_whenListAllRootCategoriesSorted_thenVerifyRepositoryCalled() {
        // Given
        Sort.Direction sortDir = Sort.Direction.ASC;

        // When
        categoryService.listAllRootCategoriesSorted("name", sortDir);

        // Then
        then(categoryRepository).should().findAllByParentIsNull(any(Sort.class));
    }

    @Test
    void givenEmptyCategoryRepository_whenListAllRootCategoriesSorted_thenVerifyRepositoryCalled() {
        // Given
        given(categoryRepository.findAllByParentIsNull(any(Sort.class))).willReturn(List.of());

        // When
        categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC);

        // Then
        then(categoryRepository).should().findAllByParentIsNull(any(Sort.class));
    }

    @Test
    void givenNewCategoryName_whenIsCategoryNameUnique_thenVerifyRepositoryCalled() {
        // Given
        given(categoryRepository.findByName(anyString())).willReturn(Optional.empty());

        // When
        categoryService.isCategoryNameUnique(null, "New Category");

        // Then
        then(categoryRepository).should().findByName(anyString());
    }

    @Test
    void givenNewCategoryAlias_whenIsCategoryAliasUnique_thenVerifyRepositoryCalled() {
        // Given
        given(categoryRepository.findByAlias(anyString())).willReturn(Optional.empty());

        // When
        categoryService.isCategoryAliasUnique(null, "new-alias");

        // Then
        then(categoryRepository).should().findByAlias(anyString());
    }


    @Test
    void givenNoKeyword_whenListCategoriesByPage_thenVerifyRootCategoriesListed() {
        // Given
        Integer pageNum = 1;
        Pageable pageable = PageRequest.of(pageNum, 5, Sort.by(Sort.Direction.ASC, "name"));
        List<Category> categories = List.of(new Category(1, "Electronics"), new Category(2, "Computers"));
        Page<Category> page = mock(Page.class);

        // Stub the page content and other pagination details
        given(page.getContent()).willReturn(categories);
        given(page.getTotalPages()).willReturn(1);
        given(page.getTotalElements()).willReturn(2L);

        // Mock the helper to return the correct pageable
        given(pagingAndSortingHelper.createPageable(5, pageNum)).willReturn(pageable);
        given(pagingAndSortingHelper.getKeyword()).willReturn(null);  // No keyword provided

        // Use doReturn to avoid the PotentialStubbingProblem
        doReturn(page).when(categoryRepository).findAllByParentIsNull(any(Pageable.class));

        // When
        categoryService.listCategoriesByPage(pageNum, pagingAndSortingHelper);

        // Then
        // Verify that the correct repository method was called for root categories
        then(categoryRepository).should().findAllByParentIsNull(any(Pageable.class));
        then(pagingAndSortingHelper).should().updateModelAttributes(eq(pageNum), eq(1), eq(2L), anyList());
    }


    // Unit test for deleteCategoryBranch method
    @Test
    void givenValidCategoryId_whenDeleteCategoryBranch_thenVerifyRepositoryCalls() throws CategoryNotFoundException {
        // Given
        Integer categoryId = 1;
        Category category = new Category(categoryId, "Category");

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // When
        categoryService.deleteCategoryBranch(categoryId);

        // Then
        then(categoryRepository).should().findById(categoryId);
        then(categoryRepository).should().deleteAllById(anyList());
    }

    @Test
    void givenNonExistingCategoryId_whenDeleteCategoryBranch_thenThrowCategoryNotFoundExceptionAndVerifyRepositoryCalled() {
        // Given
        Integer categoryId = 999;

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategoryBranch(categoryId));
        then(categoryRepository).should().findById(categoryId);
    }

    @Test
    void givenCategoryWithFile_whenSaveCategory_thenVerifyRepositoryAndFileServiceCalls() throws IOException, CategoryIllegalStateException {
        // Given
        Category category = new Category();
        category.setName("New Category");

        // Ensure category has a CategoryIcon to avoid NullPointerException
        CategoryIcon categoryIcon = new CategoryIcon();
        category.setCategoryIcon(categoryIcon);

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("image.png");
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            categoryService.saveCategory(category, file);

            // Then
            then(categoryRepository).should().save(any(Category.class));
            mockedFileService.verify(() -> FileService.saveSingleFile(eq(file), anyString(), eq("image.png")));
        }
    }

    @Test
    void givenCategoryWithoutFile_whenSaveCategory_thenVerifyRepositoryCallOnly() throws IOException, CategoryIllegalStateException {
        // Given
        Category category = new Category();
        category.setName("New Category");
        CategoryIcon categoryIcon = new CategoryIcon();
        category.setCategoryIcon(categoryIcon);

        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // When
        categoryService.saveCategory(category, null);

        // Then
        then(categoryRepository).should().save(any(Category.class));
    }

    @Test
    void givenCategoryThatReferencesItselfAsParent_whenSaveCategory_thenThrowCategoryIllegalStateExceptionAndVerifyNoSave() {
        // Given
        Category category = new Category(1, "Self-reference Category");
        category.setParent(category);
        CategoryIcon categoryIcon = new CategoryIcon();
        category.setCategoryIcon(categoryIcon);

        // When/Then
        assertThrows(CategoryIllegalStateException.class, () -> categoryService.saveCategory(category, null));
        then(categoryRepository).shouldHaveNoInteractions();
    }

    // Unit test for deleteCategoryById method
    @Test
    void givenCategoryWithNoChildren_whenDeleteCategoryById_thenVerifyRepositoryCalls() throws CategoryNotFoundException, CategoryIllegalStateException {
        // Given
        Integer categoryId = 1;
        Category category = new Category();
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // When
        categoryService.deleteCategoryById(categoryId);

        // Then
        then(categoryRepository).should().findById(categoryId);
        then(categoryRepository).should().delete(any(Category.class));
    }

    @Test
    void givenCategoryWithChildren_whenDeleteCategoryById_thenThrowCategoryIllegalStateExceptionAndVerifyNoDelete() {
        // Given
        Integer categoryId = 1;
        Category category = new Category();
        category.addChild(new Category(2, "Child Category"));

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // When/Then
        assertThrows(CategoryIllegalStateException.class, () -> categoryService.deleteCategoryById(categoryId));
        then(categoryRepository).should().findById(categoryId);
        then(categoryRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void givenNonExistingCategoryId_whenDeleteCategoryById_thenThrowCategoryNotFoundExceptionAndVerifyRepositoryCall() {
        // Given
        Integer categoryId = 999;

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategoryById(categoryId));
        then(categoryRepository).should().findById(categoryId);
    }

    // Unit test for toggleCategoryEnabledStatus method
    @Test
    void givenValidCategoryId_whenToggleCategoryEnabledStatus_thenVerifyRepositorySaveAndStatusUpdate() throws CategoryNotFoundException {
        // Given
        Integer categoryId = 1;
        Category category = new Category();
        category.addChild(new Category(2, "Child Category"));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // When
        categoryService.toggleCategoryEnabledStatus(categoryId, true);

        // Then
        then(categoryRepository).should().findById(categoryId);
        then(categoryRepository).should().save(category);
    }

    @Test
    void givenNonExistingCategoryId_whenToggleCategoryEnabledStatus_thenThrowCategoryNotFoundExceptionAndVerifyRepositoryCalled() {
        // Given
        Integer categoryId = 999;

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.toggleCategoryEnabledStatus(categoryId, true));
        then(categoryRepository).should().findById(categoryId);
    }

    @Test
    void givenCategoryHierarchy_whenExpandCategoryHierarchy_thenVerifyNamesWithCorrectPrefixes() {
        // Given
        Category rootCategory = new Category(1, "Electronics");
        Category childCategory1 = new Category(2, "Laptops");
        Category childCategory2 = new Category(3, "Gaming Laptops");

        // Set up the hierarchy
        childCategory1.setParent(rootCategory);
        childCategory2.setParent(childCategory1);

        rootCategory.addChild(childCategory1);
        childCategory1.addChild(childCategory2);

        CategoryService categoryService = new CategoryService(null, null);

        // When
        List<Category> hierarchicalCategories = categoryService.createHierarchy(List.of(rootCategory));

        // Then
        assertEquals(3, hierarchicalCategories.size(), "There should be 3 categories in the hierarchy");
        assertEquals("Electronics", hierarchicalCategories.get(0).getName(), "Root category name should be Electronics");
        assertEquals("-Laptops", hierarchicalCategories.get(1).getName(), "First child should be prefixed with one dash");
        assertEquals("--Gaming Laptops", hierarchicalCategories.get(2).getName(), "Second child should be prefixed with two dashes");
    }

    @Test
    void givenSingleCategory_whenExpandCategoryHierarchy_thenVerifyNameWithoutPrefix() {
        // Given
        Category rootCategory = new Category(1, "Electronics");

        CategoryService categoryService = new CategoryService(null, null);

        // When
        List<Category> hierarchicalCategories = categoryService.createHierarchy(List.of(rootCategory));

        // Then
        assertEquals(1, hierarchicalCategories.size(), "There should be 1 category in the hierarchy");
        assertEquals("Electronics", hierarchicalCategories.get(0).getName(), "Root category name should be Electronics without any prefix");
    }

    @Test
    void givenCategoryWithMultipleChildren_whenExpandCategoryHierarchy_thenVerifyAllNamesAreCorrectlyPrefixed() {
        // Given
        Category rootCategory = new Category(1, "Electronics");
        Category childCategory1 = new Category(2, "Laptops");
        Category childCategory2 = new Category(3, "Tablets");

        // Set up the hierarchy
        childCategory1.setParent(rootCategory);
        childCategory2.setParent(rootCategory);

        rootCategory.addChild(childCategory1);
        rootCategory.addChild(childCategory2);

        CategoryService categoryService = new CategoryService(null, null);

        // When
        List<Category> hierarchicalCategories = categoryService.createHierarchy(List.of(rootCategory));

        // Then
        assertEquals(3, hierarchicalCategories.size(), "There should be 3 categories in the hierarchy");
        assertEquals("Electronics", hierarchicalCategories.get(0).getName(), "Root category name should be Electronics");
        assertEquals("-Laptops", hierarchicalCategories.get(1).getName(), "First child should be prefixed with one dash");
        assertEquals("-Tablets", hierarchicalCategories.get(2).getName(), "Second child should be prefixed with one dash");
    }
}
