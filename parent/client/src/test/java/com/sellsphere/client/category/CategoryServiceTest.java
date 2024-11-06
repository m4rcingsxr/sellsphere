package com.sellsphere.client.category;

import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category electronics;
    private Category computers;
    private Category computerComponents;

    @BeforeEach
    void setUp() {
        electronics = new Category();
        electronics.setName("Electronics");
        electronics.setAlias("electronics");

        computers = new Category();
        computers.setName("Computers");
        computers.setAlias("computers");
        computers.setParent(electronics);

        computerComponents = new Category();
        computerComponents.setName("Computer Components");
        computerComponents.setAlias("computer_components");
        computerComponents.setParent(computers);
    }

    @Test
    void givenRootCategories_whenListRootCategories_thenReturnRootCategories() {
        // Given
        List<Category> rootCategories = List.of(electronics);
        given(categoryRepository.findAllByParentIsNullAndEnabledIsTrue()).willReturn(rootCategories);

        // When
        List<Category> categories = categoryService.listRootCategories();

        // Then
        assertThat(categories).isEqualTo(rootCategories);
    }

    @Test
    void givenAlias_whenGetCategoryByAlias_thenReturnCategory() throws CategoryNotFoundException {
        // Given
        given(categoryRepository.findByAliasAndEnabledIsTrue("computers")).willReturn(Optional.of(computers));

        // When
        Category category = categoryService.getCategoryByAlias("computers");

        // Then
        assertThat(category).isEqualTo(computers);
    }

    @Test
    void givenName_whenGetCategoryByName_thenReturnCategory() {
        // Given
        given(categoryRepository.findByName("Computer Components")).willReturn(computerComponents);

        // When
        Category category = categoryService.getCategoryByName("Computer Components");

        // Then
        assertThat(category).isEqualTo(computerComponents);
    }

    @Test
    void givenChildCategory_whenGetCategoryParents_thenReturnParentCategories() {
        // Given
        List<Category> expectedParents = List.of(electronics, computers, computerComponents);

        // When
        List<Category> parents = categoryService.getCategoryParents(computerComponents);

        // Then
        assertThat(parents).isEqualTo(expectedParents);
    }
}