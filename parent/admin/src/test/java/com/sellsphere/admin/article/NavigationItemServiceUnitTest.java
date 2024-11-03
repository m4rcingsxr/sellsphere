package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleNotFoundException;
import com.sellsphere.common.entity.NavigationItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NavigationItemServiceUnitTest {

    @Mock
    private NavigationItemRepository navigationItemRepository;

    @InjectMocks
    private NavigationItemService navigationItemService;

    @Test
    void givenExistingNavItemAndSameItemNumber_whenSave_thenDoesNotUpdateNavItem() {
        // Given
        Article article = new Article();
        article.setPublished(true);
        Integer itemNumber = 5;
        NavigationItem existingNavItem = new NavigationItem();
        existingNavItem.setArticle(article);
        existingNavItem.setItemNumber(itemNumber);

        given(navigationItemRepository.findByArticle(article)).willReturn(Optional.of(existingNavItem));

        // When
        NavigationItem savedNavItem = navigationItemService.save(article, itemNumber);

        // Then
        assertEquals(existingNavItem, savedNavItem);
        then(navigationItemRepository).should(never()).save(existingNavItem);
    }


    @Test
    void whenFindAll_thenReturnsSortedNavigationItems() {
        // Given
        List<NavigationItem> navItems = new ArrayList<>();
        Article article1 = new Article();
        Article article2 = new Article();
        NavigationItem navItem1 = new NavigationItem();
        navItem1.setArticle(article1);
        navItem1.setItemNumber(1);
        NavigationItem navItem2 = new NavigationItem();
        navItem2.setArticle(article2);
        navItem2.setItemNumber(2);
        navItems.add(navItem1);
        navItems.add(navItem2);

        given(navigationItemRepository.findAll(Sort.by(Sort.Direction.ASC, "itemNumber"))).willReturn(navItems);

        // When
        List<NavigationItem> result = navigationItemService.findAll();

        // Then
        assertEquals(2, result.size());
        then(navigationItemRepository).should(times(1)).findAll(Sort.by(Sort.Direction.ASC, "itemNumber"));
    }

    @Test
    void givenArticle_whenGetByArticle_thenReturnsNavigationItem() throws ArticleNotFoundException {
        // Given
        Article article = new Article();
        NavigationItem navItem = new NavigationItem();
        navItem.setArticle(article);
        navItem.setItemNumber(1);

        given(navigationItemRepository.findByArticle(article)).willReturn(Optional.of(navItem));

        // When
        NavigationItem foundNavItem = navigationItemService.getByArticle(article);

        // Then
        assertNotNull(foundNavItem);
        assertEquals(navItem, foundNavItem);
    }

    @Test
    void givenArticle_whenGetByArticle_thenThrowsArticleNotFoundException() {
        // Given
        Article article = new Article();
        given(navigationItemRepository.findByArticle(article)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ArticleNotFoundException.class, () -> navigationItemService.getByArticle(article));
    }

    @Test
    void givenArticle_whenDeleteByArticle_thenDeletesAssociatedNavigationItem() {
        // Given
        Article article = new Article();

        // When
        navigationItemService.deleteByArticle(article);

        // Then
        then(navigationItemRepository).should(times(1)).deleteByArticle(article);
    }
}
