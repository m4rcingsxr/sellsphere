package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleNotFoundException;
import com.sellsphere.common.entity.FooterItem;
import com.sellsphere.common.entity.FooterSection;
import com.sellsphere.common.entity.payload.ArticleMetadata;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static util.PagingTestHelper.assertSorting;

@SpringBootTest
@Transactional
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {
        "/sql/roles.sql",
        "/sql/users.sql",
        "/sql/articles.sql",
        "/sql/footer_sections.sql",
        "/sql/footer_items.sql"
})
class FooterSectionServiceIntegrationTest {

    @Autowired
    private FooterSectionService footerSectionService;

    @Autowired
    private EntityManager entityManager;


    @Test
    void givenMultipleFooterSections_whenRetrievingAll_thenShouldReturnFooterSectionsSortedBySectionNumberAsc() {
        // When
        List<FooterSection> footerSections = footerSectionService.findAll();

        // Then
        assertFalse(footerSections.isEmpty());
        assertSorting(footerSections, "sectionNumber", true);
    }

    @Test
    void givenArticleNotAssociatedWithFooterSection_whenSavingWithMetadata_thenShouldCreateNewFooterSectionAndAssociateArticle() {
        // Given
        Article newArticle = entityManager.find(Article.class, 3);
        ArticleMetadata metadata = new ArticleMetadata();
        metadata.setArticle(newArticle);
        metadata.setSectionNumber(3); // New section number
        metadata.setItemNumber(1);
        metadata.setSectionHeader(new String[]{"Header 1", "Header 2", "New Section Header"});

        // When
        FooterSection footerSection = footerSectionService.save(metadata);

        // Then
        assertNotNull(footerSection);
        assertEquals(3, footerSection.getSectionNumber());
        assertEquals("New Section Header", footerSection.getSectionHeader());
    }

    @Test
    void givenExistingFooterSectionWithSectionNumber_whenSavingWithSameSectionNumber_thenShouldUpdateSectionHeaderAndAddOrUpdateFooterItemAsNeeded() {
        // Given
        Article existingArticle = entityManager.find(Article.class, 2);
        ArticleMetadata metadata = new ArticleMetadata();
        metadata.setArticle(existingArticle);
        metadata.setSectionNumber(1);
        metadata.setItemNumber(1);
        metadata.setSectionHeader(new String[]{"Updated Header for Section 1"});

        // When
        FooterSection updatedFooterSection = footerSectionService.save(metadata);

        // Then
        assertEquals("Updated Header for Section 1", updatedFooterSection.getSectionHeader());
        assertTrue(updatedFooterSection.getFooterItems().stream()
                           .anyMatch(item -> item.getArticle().getId().equals(existingArticle.getId())));
    }

    @Test
    void givenExistingFooterSection_whenSavingWithMetadataForArticleAssociatedWithDifferentFooterItem_thenShouldRemoveArticleFromOldFooterSectionAndAssociateWithNewOne() {
        // Given
        Article article = entityManager.find(Article.class, 5);
        ArticleMetadata metadata = new ArticleMetadata();
        metadata.setArticle(article);
        metadata.setSectionNumber(3);
        metadata.setItemNumber(2);
        metadata.setSectionHeader(new String[]{"Header 1", "Header 2", "Updated Header for Section 3"});

        // When
        FooterSection updatedFooterSection = footerSectionService.save(metadata);

        // Then
        assertEquals(3, updatedFooterSection.getSectionNumber());
        assertEquals("Updated Header for Section 3", updatedFooterSection.getSectionHeader());
    }

    @Test
    void givenFooterSectionWithItemAssociatedWithArticle_whenRetrievingBySectionNumber_thenShouldReturnCorrectFooterSection() {
        // Given
        int sectionNumber = 2;

        // When
        Optional<FooterSection> footerSection = footerSectionService.get(sectionNumber);

        // Then
        assertTrue(footerSection.isPresent());
        assertEquals(2, footerSection.get().getSectionNumber());
    }

    @Test
    void givenNonExistentFooterSectionNumber_whenRetrievingBySectionNumber_thenShouldReturnEmptyOptional() {
        // Given
        int nonExistentSectionNumber = 99;

        // When
        Optional<FooterSection> footerSection = footerSectionService.get(nonExistentSectionNumber);

        // Then
        assertTrue(footerSection.isEmpty());
    }

    @Test
    void givenFooterItemAssociatedWithArticle_whenRetrievingByArticle_thenShouldReturnCorrectFooterItem()
            throws ArticleNotFoundException {
        // Given
        Article article = entityManager.find(Article.class, 6);

        // When
        FooterItem footerItem = footerSectionService.getFooterItemByArticle(article);

        // Then
        assertNotNull(footerItem);
        assertEquals(article.getId(), footerItem.getArticle().getId());
    }


}
