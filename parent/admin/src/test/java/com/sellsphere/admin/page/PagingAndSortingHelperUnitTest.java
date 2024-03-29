package com.sellsphere.admin.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PagingAndSortingHelperUnitTest {

    @Mock
    private ModelAndViewContainer model;

    @Mock
    private SearchRepository<Object, Integer> repo;

    @InjectMocks
    private PagingAndSortingHelper helper;

    @BeforeEach
    void setUp() {
        helper = new PagingAndSortingHelper(model, "entities", "name", "asc", "");
    }

    @Test
    void whenListEntitiesWithoutKeyword_thenCorrectPageRequestedAndModelUpdated() {
        // Given
        List<Object> content = Arrays.asList(new Object(), new Object());
        Page<Object> expectedPage = new PageImpl<>(content);
        given(repo.findAll(any(PageRequest.class))).willReturn(expectedPage);

        // When
        helper.listEntities(0, 5, repo);

        // Then
        then(repo).should().findAll(any(PageRequest.class));
        then(model).should().addAttribute(eq("entities"), eq(content));
    }

    @Test
    void whenListEntitiesWithKeyword_thenFilteredPageRequestedAndModelUpdated() {
        // Given
        String keyword = "test";
        helper = new PagingAndSortingHelper(model, "entities", "name", "asc", keyword);
        List<Object> content = Arrays.asList(new Object(), new Object());
        Page<Object> expectedPage = new PageImpl<>(content);
        given(repo.findAll(eq(keyword), any(PageRequest.class))).willReturn(expectedPage);

        // When
        helper.listEntities(0, 5, repo);

        // Then
        then(repo).should().findAll(eq(keyword), any(PageRequest.class));
        then(model).should().addAttribute(eq("entities"), eq(content));
    }

    @Test
    void whenListEntitiesAndPageIsEmpty_thenModelUpdatedWithEmptyList() {
        // Given
        given(repo.findAll(any(PageRequest.class))).willReturn(Page.empty());

        // When
        helper.listEntities(0, 5, repo);

        // Then
        then(repo).should().findAll(any(PageRequest.class));
        then(model).should().addAttribute(eq("entities"), eq(Collections.emptyList()));
    }

    @Test
    void whenCreatePageable_thenSortDirectionIsCorrect() {
        // Ascending
        helper = new PagingAndSortingHelper(model, "entities", "name", "asc", "");
        PageRequest pageRequestAsc = (PageRequest) helper.createPageable(5, 0);
        assertEquals(Sort.Direction.ASC, pageRequestAsc.getSort().getOrderFor("name").getDirection(), "Sort direction should be ascending");

        // Descending
        helper = new PagingAndSortingHelper(model, "entities", "name", "desc", "");
        PageRequest pageRequestDesc = (PageRequest) helper.createPageable(5, 0);
        assertEquals(Sort.Direction.DESC, pageRequestDesc.getSort().getOrderFor("name").getDirection(), "Sort direction should be descending");
    }
}