package com.sellsphere.admin.article;

import com.sellsphere.common.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static util.PagingTestHelper.assertSorting;

@SpringBootTest
@Transactional
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {"/sql/roles.sql", "/sql/users.sql","classpath:sql/articles.sql", "classpath:sql/navigation_items.sql"})
class NavigationItemServiceIntegrationTest {

    @Autowired
    private NavigationItemService navigationItemService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private NavigationItemRepository navigationItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void givenNewArticle_whenSavingNavigationItem_thenShouldCreateNewNavigationItem() {
        // Given
        Article newArticle = new Article();
        newArticle.setTitle("New Article");
        newArticle.setAlias("new-article");
        newArticle.setContent("New article content");
        newArticle.setCreatedBy(entityManager.find(User.class, 1));
        newArticle.setUpdatedTime(LocalDate.now());
        newArticle.setPublished(true);
        newArticle.setArticleImage("new-article.png");
        newArticle.setArticleType(ArticleType.NAVIGATION);
        articleRepository.save(newArticle);

        // When
        NavigationItem savedNavItem = navigationItemService.save(newArticle, 5);

        // Then
        assertNotNull(savedNavItem);
        assertEquals(5, savedNavItem.getItemNumber());
        assertEquals(newArticle.getId(), savedNavItem.getArticle().getId());
    }

    @Test
    void givenExistingArticleWithNavigationItem_whenSavingWithSameItemNumber_thenShouldNotUpdateExistingNavigationItem() {
        // Given
        Article article = entityManager.find(Article.class, 1);
        NavigationItem originalNavItem = navigationItemRepository.findByArticle(article).orElseThrow();

        // When
        NavigationItem savedNavItem = navigationItemService.save(article, originalNavItem.getItemNumber());

        // Then
        assertEquals(originalNavItem.getId(), savedNavItem.getId());
        assertEquals(originalNavItem.getItemNumber(), savedNavItem.getItemNumber());
    }

    @Test
    void givenExistingArticleWithNavigationItem_whenSavingWithDifferentItemNumber_thenShouldUpdateNavigationItemNumber() {
        // Given
        Article article = entityManager.find(Article.class, 2);

        // When
        NavigationItem updatedNavItem = navigationItemService.save(article, 10);

        // Then
        assertEquals(10, updatedNavItem.getItemNumber());
    }

    @Test
    void givenMultipleNavigationItems_whenRetrievingAll_thenShouldReturnItemsSortedByItemNumberAsc() {
        // When
        List<NavigationItem> navigationItems = navigationItemService.findAll();

        // Then
        assertEquals(4, navigationItems.size());
        assertSorting(navigationItems, "itemNumber", true);
    }

    @Test
    void givenArticleWithNavigationItem_whenRetrievingByArticle_thenShouldReturnCorrectNavigationItem()
            throws ArticleNotFoundException {
        // Given
        Article article = entityManager.find(Article.class, 1);

        // When
        NavigationItem navItem = navigationItemService.getByArticle(article);

        // Then
        assertNotNull(navItem);
        assertEquals(article.getId(), navItem.getArticle().getId());
    }

    @Test
    void givenArticleWithoutNavigationItem_whenRetrievingByArticle_thenShouldThrowArticleNotFoundException() {
        // Given
        Article article = entityManager.find(Article.class, 6);

        // When & Then
        assertThrows(ArticleNotFoundException.class, () -> navigationItemService.getByArticle(article));
    }

    @Test
    void givenArticleWithNavigationItem_whenDeletingByArticle_thenShouldRemoveAssociatedNavigationItem() {
        // Given
        Article article = entityManager.find(Article.class, 1);

        // When
        navigationItemService.deleteByArticle(article);
        Optional<NavigationItem> deletedNavItem = navigationItemRepository.findByArticle(article);

        // Then
        assertTrue(deletedNavItem.isEmpty());
    }

}
