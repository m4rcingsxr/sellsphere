package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProductFilterArgumentResolverTest {

    private ProductFilterArgumentResolver resolver;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new ProductFilterArgumentResolver(categoryRepository);
    }

    @Test
    void testResolveArgument_WithValidCategoryAlias() throws Exception {
        when(webRequest.getParameter("category_alias")).thenReturn("electronics");
        when(webRequest.getParameter("keyword")).thenReturn(null);
        when(webRequest.getParameterValues("filter")).thenReturn(new String[]{"new", "popular"});
        when(webRequest.getParameter("pageNum")).thenReturn("1");

        Category category = new Category();
        category.setId(1);
        when(categoryRepository.findByAliasAndEnabledIsTrue("electronics")).thenReturn(Optional.of(category));

        ProductPageRequest result = (ProductPageRequest) resolver.resolveArgument(methodParameter, mavContainer, webRequest, null);

        assertNotNull(result);
        assertArrayEquals(new String[]{"new", "popular"}, result.getFilter());
        assertEquals("electronics", result.getCategoryAlias());
        assertNull(result.getKeyword());
        assertEquals(1, result.getPageNum());
        assertEquals(1, result.getCategoryId());
    }

    @Test
    void testResolveArgument_WithValidKeyword() throws Exception {
        when(webRequest.getParameter("category_alias")).thenReturn(null);
        when(webRequest.getParameter("keyword")).thenReturn("laptop");
        when(webRequest.getParameterValues("filter")).thenReturn(new String[]{"discounted"});
        when(webRequest.getParameter("pageNum")).thenReturn("2");

        ProductPageRequest result = (ProductPageRequest) resolver.resolveArgument(methodParameter, mavContainer, webRequest, null);

        assertNotNull(result);
        assertArrayEquals(new String[]{"discounted"}, result.getFilter());
        assertNull(result.getCategoryAlias());
        assertEquals("laptop", result.getKeyword());
        assertEquals(2, result.getPageNum());
        assertNull(result.getCategoryId());
    }

    @Test
    void testResolveArgument_MissingCategoryAliasAndKeyword() {
        when(webRequest.getParameter("category_alias")).thenReturn(null);
        when(webRequest.getParameter("keyword")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, null);
        });
    }

    @Test
    void testResolveArgument_CategoryNotFound() {
        when(webRequest.getParameter("category_alias")).thenReturn("nonexistent");
        when(webRequest.getParameter("keyword")).thenReturn(null);

        when(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, null);
        });
    }
}
