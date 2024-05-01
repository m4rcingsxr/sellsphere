package com.sellsphere.client.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.core.MethodParameter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFilterArgumentResolverTest {

    private ProductFilterArgumentResolver resolver;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private WebDataBinderFactory binderFactory;

    @BeforeEach
    void setUp() {
        resolver = new ProductFilterArgumentResolver();
    }

    @Test
    void givenProductFilterAnnotation_whenSupportsParameter_thenReturnsTrue() {
        when(methodParameter.getParameterAnnotation(ProductFilter.class)).thenReturn(mock(ProductFilter.class));
        assertTrue(resolver.supportsParameter(methodParameter));
    }

    @Test
    void givenNoProductFilterAnnotation_whenSupportsParameter_thenReturnsFalse() {
        when(methodParameter.getParameterAnnotation(ProductFilter.class)).thenReturn(null);
        assertFalse(resolver.supportsParameter(methodParameter));
    }

    @Test
    void givenMissingCategoryAliasAndKeyword_whenResolveArgument_thenThrowsException() {
        when(webRequest.getParameter("category_alias")).thenReturn(null);
        when(webRequest.getParameter("keyword")).thenReturn(null);
        when(webRequest.getParameter("pageNum")).thenReturn("1");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        });

        assertEquals("Either categoryAlias or keyword must be provided", exception.getMessage());
    }

    @Test
    void givenMissingPageNum_whenResolveArgument_thenThrowsException() {
        when(webRequest.getParameter("category_alias")).thenReturn("someAlias");
        when(webRequest.getParameter("keyword")).thenReturn(null);
        when(webRequest.getParameter("pageNum")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        });

        assertEquals("pageNum must be provided", exception.getMessage());
    }

    @Test
    void givenValidParameters_whenResolveArgument_thenReturnsProductPageRequest() throws Exception {
        when(webRequest.getParameterValues("filter")).thenReturn(new String[] {"RAM,16gb", "Processor,intel i7"});
        when(webRequest.getParameter("category_alias")).thenReturn("someAlias");
        when(webRequest.getParameter("keyword")).thenReturn("someKeyword");
        when(webRequest.getParameter("pageNum")).thenReturn("1");

        ProductPageRequest params = (ProductPageRequest) resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        assertNotNull(params);
        assertArrayEquals(new String[] {"RAM,16gb", "Processor,intel i7"}, params.getFilter());
        assertEquals("someAlias", params.getCategoryAlias());
        assertEquals("someKeyword", params.getKeyword());
        assertEquals(1, params.getPageNum());
    }
}