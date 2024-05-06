package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.Category;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFilterArgumentResolverTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private ProductFilterArgumentResolver resolver;

    private NativeWebRequest webRequest;
    private MockHttpServletRequest servletRequest;


    @Test
    void givenValidQueryParameters_whenResolveArgument_thenShouldReturnCorrectProductPageRequest() throws Exception {
        servletRequest.setRequestURI("/products");
        servletRequest.setParameter("pageNum", "1");
        servletRequest.setParameter("minPrice", "10.0");
        servletRequest.setParameter("maxPrice", "100.0");

        when(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).thenReturn(Optional.of(new Category()));
        when(validator.validate(any(ProductPageRequest.class))).thenReturn(Collections.emptySet());

        ProductPageRequest result = (ProductPageRequest) resolver.resolveArgument(null, null, webRequest, null);

        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(BigDecimal.valueOf(10.0), result.getMinPrice());
        assertEquals(BigDecimal.valueOf(100.0), result.getMaxPrice());
    }

    @Test
    void givenValidQueryParameters_whenResolveArgument_thenShouldReturnCorrectMapCountRequest() throws Exception {
        servletRequest.setRequestURI("/filter_counts");
        servletRequest.setParameter("minPrice", "5.0");
        servletRequest.setParameter("maxPrice", "50.0");

        when(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).thenReturn(Optional.of(new Category()));
        when(validator.validate(any(FilterMapCountRequest.class))).thenReturn(Collections.emptySet());

        FilterMapCountRequest result = (FilterMapCountRequest) resolver.resolveArgument(null, null, webRequest, null);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5.0), result.getMinPrice());
        assertEquals(BigDecimal.valueOf(50.0), result.getMaxPrice());
    }

    @Test
    void givenNotValidParameters_whenResolveArgument_thenShouldThrowExceptionWithExpectedViolations() throws Exception {
        servletRequest.setRequestURI("/products");
        servletRequest.setParameter("pageNum", "1");

        ConstraintViolation<ProductPageRequest> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<ProductPageRequest>> violations = Collections.singleton(violation);

        when(categoryRepository.findByAliasAndEnabledIsTrue(anyString())).thenReturn(Optional.of(new Category()));
        when(validator.validate(any(ProductPageRequest.class))).thenReturn(violations);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () ->
                resolver.resolveArgument(null, null, webRequest, null)
        );

        assertEquals(violations, exception.getConstraintViolations());
    }
}
