package com.sellsphere.admin.category;


import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.sellsphere.admin.category.TestCategoryHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileService fileService;

    @Mock
    private PagingAndSortingHelper helper;

    private final List<Category> rootCategories = generateRootCategories();


    @Test
    void whenListAllRootCategoriesSorted_thenSortedCategoriesInHierarchicalOrderReturned() {

        // Given
        when(categoryRepository.findAllByParentIsNull(any(Sort.class))).thenReturn(rootCategories);

        // When
        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", "asc");

        // Then
        assertRootCategoriesSortedByName(categoryList);
        categoryList.forEach(category -> assertHierarchy(category, rootCategories));
    }


    @Test
    void givenKeyword_whenPageCategories_thenUseSearchRepository() {

        // Given
        Integer pageNum = 0;
        String keyword = "TestKeyword";
        when(helper.createPageable(CategoryService.CATEGORY_PER_PAGE, pageNum)).thenReturn(
                PageRequest.of(pageNum, CategoryService.CATEGORY_PER_PAGE));
        when(helper.getKeyword()).thenReturn(keyword);

        // When
        categoryService.listPage(pageNum, helper);

        // Then
        verify(helper).listEntities(eq(pageNum), eq(CategoryService.CATEGORY_PER_PAGE), any());
    }

    @Test
    void givenNoKeyword_whenListingCategories_thenListRootCategories() {

        // Given
        int pageNum = 1;
        Page<Category> mockPage = Page.empty();
        when(helper.createPageable(CategoryService.CATEGORY_PER_PAGE, pageNum)).thenReturn(
                PageRequest.of(pageNum, CategoryService.CATEGORY_PER_PAGE));
        when(helper.getKeyword()).thenReturn(null);
        when(categoryRepository.findAllByParentIsNull(any(Pageable.class))).thenReturn(mockPage);

        // When
        categoryService.listPage(pageNum, helper);

        // Then
        verify(helper).updateModelAttributes(anyInt(), anyInt(), anyLong(), anyList());
    }

    @Test
    void givenCategoryWithoutParent_whenSaving_thenHierarchyPathShouldNotUpdate()
            throws CategoryIllegalStateException, IOException {
        // Given
        Category newCategory = new Category();

        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        // When
        Category savedCategory = categoryService.save(newCategory, null);

        // Then
        assertNull(savedCategory.getAllParentIDs(),
                   "Category without a parent should not have a hierarchy path."
        );
    }

    @Test
    void givenCategoryWithParent_whenSaving_thenHierarchyPathShouldUpdateCorrectly()
            throws CategoryIllegalStateException {

        // Given
        Category parentCategory = new Category();
        parentCategory.setId(1);

        Category category = new Category();
        category.setId(2);
        category.setParent(parentCategory);

        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));


        // When
        Category savedCategory = categoryService.save(category);

        // Then
        assertEquals("-1-", savedCategory.getAllParentIDs());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void givenFile_whenSavingCategory_thenFileShouldBeSaved()
            throws IOException, CategoryIllegalStateException {
        // Given
        Category parentCategory = new Category();
        parentCategory.setId(1);

        Category category = new Category();
        category.setId(2);
        category.setParent(parentCategory);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.jpg");

        // When saving category, return the same category (mock save behavior)
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        // When
        Category savedCategory = categoryService.save(category, file);

        // Verify the file saving
        verify(fileService, times(1)).saveSingleFile(file, "category-photos/2", "test.jpg");
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void givenCategoryThatReferencesItself_whenSaving_thenShouldThrowException() {
        // Given
        Category category = new Category();
        category.setId(1);
        category.setParent(category);

        // When & Then
        assertThrows(CategoryIllegalStateException.class, () -> {
            categoryService.save(category);
        });

        // Verify that the repository save method was never called
        verify(categoryRepository, never()).save(any(Category.class));
    }

}
