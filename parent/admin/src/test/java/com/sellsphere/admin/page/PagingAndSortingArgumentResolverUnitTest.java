package com.sellsphere.admin.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PagingAndSortingArgumentResolverUnitTest {

    @InjectMocks
    private PagingAndSortingArgumentResolver resolver;

    @Mock
    private MethodParameter parameter;

    @Mock
    private ModelAndViewContainer model;

    @Mock
    private NativeWebRequest request;

    @Mock
    private WebDataBinderFactory binderFactory;

    @BeforeEach
    void setUp() {
        given(parameter.getParameterAnnotation(PagingAndSortingParam.class)).willReturn(mock(PagingAndSortingParam.class));
    }

    @Test
    void supportsParameter_WhenAnnotated_ShouldReturnTrue() {
        // Given
        given(parameter.getParameterAnnotation(PagingAndSortingParam.class)).willReturn(mock(PagingAndSortingParam.class));

        // When
        boolean supports = resolver.supportsParameter(parameter);

        // Then
        assertTrue(supports);
    }

    @Test
    void resolveArgument_WhenParamsProvided_ShouldPopulateHelper() throws Exception {
        // Given
        given(request.getParameter("sortDir")).willReturn("asc");
        given(request.getParameter("sortField")).willReturn("name");
        given(request.getParameter("keyword")).willReturn("test");
        PagingAndSortingParam annotation = mock(PagingAndSortingParam.class);
        given(annotation.listName()).willReturn("testList");
        given(annotation.moduleURL()).willReturn("/testModule");
        given(parameter.getParameterAnnotation(PagingAndSortingParam.class)).willReturn(annotation);

        // When
        PagingAndSortingHelper helper = (PagingAndSortingHelper) resolver.resolveArgument(parameter, model, request, binderFactory);

        // Then
        assertNotNull(helper);
        assertEquals("name", helper.getSortField());
        assertEquals("asc", helper.getSortDir());
        assertEquals("test", helper.getKeyword());
        then(model).should().addAttribute("sortField", "name");
        then(model).should().addAttribute("sortDir", "asc");
        then(model).should().addAttribute("reversedSortDir", "desc");
        then(model).should().addAttribute("keyword", "test");
        then(model).should().addAttribute("moduleURL", "/testModule");
    }

}