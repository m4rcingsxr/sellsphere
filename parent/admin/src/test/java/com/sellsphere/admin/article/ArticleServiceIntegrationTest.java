package com.sellsphere.admin.article;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.FileService;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.ArticleMetadata;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.S3TestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.PagingTestHelper.assertSorting;

@SpringBootTest
@Transactional
@ExtendWith(S3MockExtension.class)
@org.springframework.transaction.annotation.Transactional
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {
        "/sql/roles.sql",
        "/sql/users.sql",
        "/sql/articles.sql",
        "/sql/navigation_items.sql",
        "/sql/footer_sections.sql",
        "/sql/footer_items.sql",
        "/sql/promotions.sql"
})
class ArticleServiceIntegrationTest {

    private static final String S3_BUCKET_NAME = "test-bucket";
    private static S3Client s3Client;

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private PromotionService promotionService;
    @Autowired
    private NavigationItemRepository navigationItemRepository;
    @Autowired
    private FooterSectionService footerSectionService;
    @Autowired
    private EntityManager entityManager;

    @BeforeAll
    static void setUpS3(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(S3_BUCKET_NAME);
        S3Utility.setS3Client(s3Client);
        S3TestUtils.createBucket(s3Client, S3_BUCKET_NAME);
    }

    @Test
    void givenPageNumber_whenListPage_thenReturnArticlesForSpecificPage() {
        int pageNum = 1;
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "articleList", "title",
                                                                   Sort.Direction.ASC, null
        );

        articleService.listPage(helper, pageNum);

        assertEquals(4, helper.getContent().size());
        assertSorting(helper.getContent(), "title", true);
    }

    @Test
    void givenPromotionArticleWithImage_whenSaveArticle_thenSavePromotionWithLinkedImageInS3() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);

        MockMultipartFile image = new MockMultipartFile("file", "promo-image.jpg", "image/jpeg", "Promo Image".getBytes());

        Article article = new Article();
        article.setTitle("Promotion Article");
        article.setAlias("promotion-article");
        article.setContent("This is content for the promotion article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.PROMOTION);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .promotionName("Special Promotion")
                .selectedProducts(List.of(1, 2))
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, image);

        // Then
        assertNotNull(savedArticle.getPromotion());
        S3TestUtils.verifyFileContent(
                s3Client,
                S3_BUCKET_NAME,
                "article-photos/" + savedArticle.getId() + "/promo-image.jpg",
                image.getInputStream()
        );
    }

    @Test
    void givenExistingPromotionName_whenSavePromotionArticle_thenAddArticleToExistingPromotion() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);

        MockMultipartFile image = new MockMultipartFile("file", "existing-promo.jpg", "image/jpeg", "Existing Promo".getBytes());

        Article article = new Article();
        article.setTitle("Existing Promotion Article");
        article.setAlias("existing-promotion-article");
        article.setContent("Content for an existing promotion article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.PROMOTION);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .promotionName("Gamer Gear Deals")
                .selectedProducts(List.of(1, 2))
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, image);

        // Then
        assertEquals("Gamer Gear Deals", savedArticle.getPromotion().getName());
    }

    @Test
    void givenFooterArticle_whenSaveArticle_thenUpdateFooterSectionWithNewArticle() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);
        Article article = new Article();
        article.setTitle("Footer Article");
        article.setAlias("footer-article");
        article.setContent("Content for the footer article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.FOOTER);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .sectionNumber(1)
                .itemNumber(1)
                .sectionHeader(new String[]{"Footer Section Header", "2"})
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, null);

        // Then
        FooterSection footerSection = footerSectionService.get(1).orElseThrow();
        assertTrue(
                footerSection.getFooterItems().stream()
                        .anyMatch(item -> item.getArticle().getId().equals(savedArticle.getId()))
        );
        assertEquals("Footer Section Header", footerSection.getSectionHeader());
    }

    @Test
    void givenFreeArticleType_whenSaveArticle_thenUnlinkFromNavigationAndFooter() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);
        Article article = new Article();
        article.setTitle("Free Article");
        article.setAlias("free-article");
        article.setContent("Content for a free article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.FREE);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .sectionNumber(1)
                .itemNumber(1)
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, null);

        // Then
        assertEquals(ArticleType.FREE, savedArticle.getArticleType());
        assertTrue(navigationItemRepository.findByArticle(article).isEmpty());
        assertTrue(footerSectionService.get(metadata.getSectionNumber())
                           .orElseThrow()
                           .getFooterItems()
                           .stream()
                           .noneMatch(item -> item.getArticle().getId().equals(savedArticle.getId())));
    }

    @Test
    void givenArticleWithExistingImage_whenSaveArticle_thenReplaceImageInS3() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);

        // Initial setup with an existing image
        Article article = new Article();
        article.setTitle("Article with Image");
        article.setAlias("article-with-image");
        article.setContent("This article initially had an image.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.FOOTER);
        article.setCreatedBy(user);
        article.setArticleImage("old-image.jpg"); // Simulate existing image

        // Simulate saving the original image in S3
        MockMultipartFile initialImage = new MockMultipartFile("file", "old-image.jpg", "image/jpeg", "Old Image Content".getBytes());
        FileService.saveSingleFile(initialImage, "article-photos/" + article.getId(), "old-image.jpg");

        // Now prepare the new image to replace the existing one
        MockMultipartFile newImage = new MockMultipartFile("file", "new-image.jpg", "image/jpeg", "New Image Content".getBytes());

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .sectionNumber(1)
                .itemNumber(2)
                .sectionHeader(new String[]{"Existing Section Header", "2"})
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, newImage);

        // Then
        assertEquals("new-image.jpg", savedArticle.getArticleImage());
        S3TestUtils.verifyFileContent(
                s3Client,
                S3_BUCKET_NAME,
                "article-photos/" + savedArticle.getId() + "/new-image.jpg",
                newImage.getInputStream()
        );
    }

    @Test
    void givenNavigationArticleType_whenSaveArticle_thenAddArticleToNavigation() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);
        Article article = new Article();
        article.setTitle("Navigation Article");
        article.setAlias("navigation-article");
        article.setContent("Content for a navigation article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.NAVIGATION);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .itemNumber(1)
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, null);

        // Then
        assertEquals(ArticleType.NAVIGATION, savedArticle.getArticleType());
        assertTrue(navigationItemRepository.findByArticle(savedArticle).isPresent());
    }

    @Test
    void givenArticleTypeAndSortField_whenFindAllByArticleType_thenReturnSortedArticles() {
        // Given
        ArticleType articleType = ArticleType.FOOTER;
        String sortField = "title";
        Sort.Direction direction = Sort.Direction.ASC;

        // When
        List<Article> articles = articleService.findAllByArticleType(articleType, sortField, direction);

        // Then
        assertFalse(articles.isEmpty());
        assertSorting(articles, sortField, true);
    }

    @Test
    void givenArticleId_whenGetArticle_thenReturnCorrectArticle() throws ArticleNotFoundException {
        // Given
        int articleId = 1;

        // When
        Article article = articleService.get(articleId);

        // Then
        assertNotNull(article);
        assertEquals(articleId, article.getId());
    }

    @Test
    void givenNonExistingArticleId_whenGetArticle_thenThrowArticleNotFoundException() {
        // Given
        int nonExistentId = 999;

        // Then
        assertThrows(ArticleNotFoundException.class, () -> articleService.get(nonExistentId));
    }

    @Test
    void givenUniqueTitle_whenIsArticleNameUnique_thenReturnTrue() {
        // Given
        String uniqueTitle = "Unique Article Title";

        // When
        boolean isUnique = articleService.isArticleNameUnique(null, uniqueTitle);

        // Then
        assertTrue(isUnique);
    }

    @Test
    void givenDuplicateTitleWithDifferentId_whenIsArticleNameUnique_thenReturnFalse() {
        // Given
        String duplicateTitle = "Top 10 Laptops for 2024";
        Article existingArticle = articleRepository.findByTitle(duplicateTitle).orElseThrow();
        int differentId = existingArticle.getId() + 1;

        // When
        boolean isUnique = articleService.isArticleNameUnique(differentId, duplicateTitle);

        // Then
        assertFalse(isUnique);
    }

    @Test
    void givenExistingArticleWithSameId_whenIsArticleNameUnique_thenReturnTrue() {
        // Given
        String existingTitle = "Latest Smartphone Releases";
        Article existingArticle = articleRepository.findByTitle(existingTitle).orElseThrow();

        // When
        boolean isUnique = articleService.isArticleNameUnique(existingArticle.getId(), existingTitle);

        // Then
        assertTrue(isUnique);
    }

    @Test
    void givenNewPromotionArticle_whenSave_thenLinkToPromotionAndProducts() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);
        Article article = new Article();
        article.setTitle("New Promotion Article");
        article.setAlias("new-promotion-article");
        article.setContent("Content for a new promotion article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.PROMOTION);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .promotionName("Special Deal")
                .selectedProducts(List.of(1, 2))
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, null);

        // Then
        assertNotNull(savedArticle.getPromotion());
        assertEquals("Special Deal", savedArticle.getPromotion().getName());
    }

    @Test
    void givenNonPromotionArticle_whenDeleteArticle_thenRemoveOnlyFromArticleRepo() throws ArticleNotFoundException {
        // Given
        int articleId = 3;

        // When
        articleService.deleteArticle(articleId);

        // Then
        assertFalse(articleRepository.findById(articleId).isPresent());
    }

    @Test
    void givenPromotionArticle_whenDeleteArticle_thenUnlinkPromotionDetailsAndDelete()
            throws ArticleNotFoundException, PromotionNotFoundException {
        // Given
        int promotionArticleId = 4;

        // When
        articleService.deleteArticle(promotionArticleId);

        // Then
        assertFalse(articleRepository.findById(promotionArticleId).isPresent());
        assertFalse(promotionService.getById(1).getProducts().stream()
                            .anyMatch(product -> product.getDetails()
                                    .stream()
                                    .anyMatch(detail -> detail.getValue()
                                            .contains("Promotion"))));
    }

    @Test
    void givenFooterArticleMetadata_whenHandleArticleTypeSpecifics_thenLinkToFooterSection() throws IOException {
        // Given
        User user = entityManager.find(User.class, 1);
        Article article = new Article();
        article.setTitle("Footer Specific Article");
        article.setAlias("footer-specific-article");
        article.setContent("Content for a footer specific article.");
        article.setUpdatedTime(LocalDate.now());
        article.setPublished(true);
        article.setArticleType(ArticleType.FOOTER);
        article.setCreatedBy(user);

        ArticleMetadata metadata = ArticleMetadata.builder()
                .article(article)
                .user(user)
                .sectionNumber(1)
                .itemNumber(1)
                .sectionHeader(new String[]{"Footer Header"})
                .build();

        // When
        Article savedArticle = articleService.saveArticle(metadata, null);

        // Then
        FooterSection footerSection = footerSectionService.get(1).orElseThrow();
        assertTrue(footerSection.getFooterItems().stream()
                           .anyMatch(item -> item.getArticle().getId().equals(savedArticle.getId())));
        assertEquals("Footer Header", footerSection.getSectionHeader());
    }



    @Test
    void givenArticlesWithTitles_whenFindAllByTitles_thenReturnMatchingArticles() {
        // Given
        List<String> titles = List.of("Gaming Accessories You Need", "4K TVs: What to Look For");

        // When
        List<Article> articles = articleService.findAllByTitles(titles);

        // Then
        assertEquals(2, articles.size());
        assertTrue(articles.stream().allMatch(article -> titles.contains(article.getTitle())));
    }


}