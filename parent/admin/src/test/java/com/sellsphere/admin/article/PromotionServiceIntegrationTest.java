package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import com.sellsphere.common.entity.Promotion;
import com.sellsphere.common.entity.PromotionNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {
        "/sql/roles.sql",
        "/sql/users.sql",
        "/sql/articles.sql",
        "/sql/promotions.sql"
})
class PromotionServiceIntegrationTest {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void givenExistingPromotionName_whenGetByName_thenShouldReturnPromotion() throws PromotionNotFoundException {
        String existingPromotionName = "Gamer Gear Deals";
        Promotion promotion = promotionService.getByName(existingPromotionName);
        assertNotNull(promotion);
        assertEquals(existingPromotionName, promotion.getName());
    }

    @Test
    void givenNonExistingPromotionName_whenGetByName_thenShouldThrowPromotionNotFoundException() {
        String nonExistentName = "NonExistentPromotion";
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getByName(nonExistentName));
    }

    @Test
    void whenSave_thenShouldReturnSavedPromotion() {
        Article associatedArticle1 = entityManager.find(Article.class, 6);
        Article associatedArticle2 = entityManager.find(Article.class, 7);

        Promotion newPromotion = new Promotion();
        newPromotion.setName("New Promotion");

        // Add multiple articles to the promotion
        newPromotion.addArticle(associatedArticle1);
        newPromotion.addArticle(associatedArticle2);

        Promotion savedPromotion = promotionService.save(newPromotion);

        assertNotNull(savedPromotion);
        assertEquals("New Promotion", savedPromotion.getName());
        assertEquals(2, savedPromotion.getArticles().size()); // Verify the number of associated articles
    }

    @Test
    void givenExistingArticle_whenGetByArticle_thenShouldReturnPromotion() throws PromotionNotFoundException {
        Article associatedArticle = entityManager.find(Article.class, 4);
        Promotion promotion = promotionService.getByArticle(associatedArticle);
        assertNotNull(promotion);
        assertTrue(promotion.getArticles().contains(associatedArticle));
    }

    @Test
    void givenArticleWithNotPromotionType_whenGetByArticle_thenShouldThrowPromotionNotFoundException() {
        Article nonAssociatedArticle = entityManager.find(Article.class, 1);
        assertNotEquals(nonAssociatedArticle.getArticleType(), ArticleType.PROMOTION);
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getByArticle(nonAssociatedArticle));
    }

    @Test
    void whenListAll_thenShouldReturnPromotionsSortedByName() {
        List<Promotion> promotions = promotionService.listAll();
        assertFalse(promotions.isEmpty());
        assertTrue(promotions.get(0).getName().compareTo(promotions.get(1).getName()) < 0);
    }

    @Test
    void whenDelete_thenShouldRemovePromotion() throws PromotionNotFoundException {
        Promotion promotionToDelete = promotionService.getById(1);
        promotionService.delete(promotionToDelete);
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getById(1));
    }

    @Test
    void givenExistingPromotionId_whenGetById_thenShouldReturnPromotion() throws PromotionNotFoundException {
        int promotionId = 2;
        Promotion promotion = promotionService.getById(promotionId);
        assertNotNull(promotion);
        assertEquals(promotionId, promotion.getId());
    }

    @Test
    void givenNonExistingPromotionId_whenGetById_thenShouldThrowPromotionNotFoundException() {
        int nonExistentPromotionId = 99;
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getById(nonExistentPromotionId));
    }
}
