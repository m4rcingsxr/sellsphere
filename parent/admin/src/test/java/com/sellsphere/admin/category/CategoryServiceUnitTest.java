package com.sellsphere.admin.category;


import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.sellsphere.admin.category.TestCategoryHelper.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

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


}
