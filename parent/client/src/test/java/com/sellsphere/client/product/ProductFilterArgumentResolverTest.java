package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFilterArgumentResolverTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductFilterArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ProductFilterArgumentResolver(categoryRepository);
    }

    @Test
    void shouldSupportProductFilterAnnotation() {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getParameterAnnotation(ProductFilter.class)).thenReturn(mock(ProductFilter.class));

        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void shouldNotSupportWithoutProductFilterAnnotation() {
        MethodParameter parameter = mock(MethodParameter.class);

        assertFalse(resolver.supportsParameter(parameter));
    }



    @Test
    void shouldResolveArgumentForFilterMapCountRequest() throws Exception {
        NativeWebRequest webRequest = prepareWebRequest("filter_counts");
        when(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).thenReturn(Optional.of(mockCategory(1)));

        FilterMapCountRequest mapRequest = (FilterMapCountRequest) resolver.resolveArgument(mock(MethodParameter.class), null, webRequest, null);

        assertNotNull(mapRequest);
        assertEquals("someAlias", mapRequest.getCategoryAlias());
        assertEquals(1, mapRequest.getCategoryId());
    }

    @Test
    void shouldThrowConstraintViolationExceptionForInvalidProductPageRequest() {
        NativeWebRequest webRequest = prepareInvalidWebRequest("products");

        assertThrows(ConstraintViolationException.class, () -> resolver.resolveArgument(mock(MethodParameter.class), null, webRequest, null));
    }


    @Test
    void shouldThrowCategoryNotFoundException() throws Exception {
        NativeWebRequest webRequest = prepareWebRequest("products");
        lenient().when(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> resolver.resolveArgument(mock(MethodParameter.class), null, webRequest, null));
    }

    private NativeWebRequest prepareWebRequest(String endpoint) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(endpoint);
        request.setParameter("category_alias", "someAlias");
        request.setParameter("pageNum", "1");

        return new ServletWebRequest(request);
    }

    private NativeWebRequest prepareInvalidWebRequest(String endpoint) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(endpoint);

        return new ServletWebRequest(request);
    }

    private Category mockCategory(int id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
