package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/sql/roles.sql", "/sql/users.sql", "/sql/articles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void givenExistingArticles_whenFindAllByArticleType_thenReturnCorrectArticles() {
        List<Article> navigationArticles = articleRepository.findAllByArticleType(ArticleType.NAVIGATION, null);
        assertEquals(4, navigationArticles.size());
    }

    @Test
    void givenTitles_whenFindAllByTitles_thenReturnMatchingArticles() {
        List<Article> foundArticles = articleRepository.findAllByTitleIn(Arrays.asList("Latest Smartphone Releases", "Top 10 Laptops for 2024"));
        assertEquals(2, foundArticles.size());
        assertTrue(foundArticles.stream().anyMatch(article -> article.getTitle().equals("Latest Smartphone Releases")));
        assertTrue(foundArticles.stream().anyMatch(article -> article.getTitle().equals("Top 10 Laptops for 2024")));
    }

    @Test
    void givenNonExistentTitles_whenFindAllByTitles_thenReturnEmptyList() {
        List<Article> foundArticles = articleRepository.findAllByTitleIn(Arrays.asList("Non-existent Title", "Another Non-existent Title"));
        assertTrue(foundArticles.isEmpty());
    }

    @Test
    void givenExistingTitle_whenFindByTitle_thenReturnArticle() {
        Optional<Article> foundArticle = articleRepository.findByTitle("Latest Smartphone Releases");
        assertTrue(foundArticle.isPresent());
        assertEquals("Latest Smartphone Releases", foundArticle.get().getTitle());
    }

    @Test
    void givenNonExistentTitle_whenFindByTitle_thenReturnEmptyOptional() {
        Optional<Article> foundArticle = articleRepository.findByTitle("Non-existent Title");
        assertFalse(foundArticle.isPresent());
    }

    @Test
    void givenKeyword_whenFindAllWithKeyword_thenReturnMatchingArticles() {
        var articles = articleRepository.findAll("smartphone", null);
        assertEquals(1, articles.getContent().size());
        assertEquals("Latest Smartphone Releases", articles.getContent().get(0).getTitle());
    }

    @Test
    void givenNonMatchingKeyword_whenFindAllWithKeyword_thenReturnEmptyResult() {
        var articles = articleRepository.findAll("nonexistent", null);
        assertTrue(articles.getContent().isEmpty());
    }

    @Test
    void givenPaginationAndSorting_whenFindAll_thenReturnCorrectlyOrderedArticles() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "title"));
        var articles = articleRepository.findAll(pageRequest);
        PagingTestHelper.assertPagingResults(articles, 5, 2, 10, "title", true);
    }
}
