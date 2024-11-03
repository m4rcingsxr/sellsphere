package com.sellsphere.admin.article;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.ArticleMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceUnitTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private NavigationItemService navigationItemService;

    @Mock
    private FooterSectionService footerSectionService;

    @Mock
    private PromotionService promotionService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MultipartFile newImage;

    @InjectMocks
    private ArticleService articleService;

    private Article article;
    private ArticleMetadata articleMetadata;

    @BeforeEach
    void setup() {
        article = new Article();
        article.setId(1);
        article.setTitle("Test Article");
        article.setArticleType(ArticleType.FREE);

        articleMetadata = ArticleMetadata.builder()
                .article(article)
                .promotionName("New Promotion")
                .sectionNumber(1)
                .itemNumber(1)
                .selectedProducts(List.of(1, 2))
                .build();
    }

    @Test
    void givenFreeArticleType_whenSaveArticle_thenHandlesFreeArticleTypeSpecifics() throws IOException {
        // Given
        article.setArticleType(ArticleType.FREE);
        articleMetadata.setArticle(article);
        given(articleRepository.save(article)).willReturn(article);

        // Create a mock FooterSection and link it with a mutable list of FooterItems
        FooterSection mockFooterSection = new FooterSection();
        FooterItem footerItem = new FooterItem();
        footerItem.setArticle(article);
        footerItem.setFooterSection(mockFooterSection);
        mockFooterSection.setFooterItems(new ArrayList<>(List.of(footerItem)));

        given(footerSectionService.get(articleMetadata.getSectionNumber())).willReturn(Optional.of(mockFooterSection));

        // When
        Article savedArticle = articleService.saveArticle(articleMetadata, null);

        // Then
        assertNotNull(savedArticle);
        assertEquals(ArticleType.FREE, savedArticle.getArticleType());

        // Verify interactions with navigation and footer services
        then(navigationItemService).should().deleteByArticle(article);
        then(footerSectionService).should().get(articleMetadata.getSectionNumber());
        then(footerSectionService).should().save(mockFooterSection); // Verify that save is called on the linked section
    }

    @Test
    void givenNavigationArticleType_whenSaveArticle_thenHandlesNavigationArticleTypeSpecifics() throws IOException {
        // Given
        article.setArticleType(ArticleType.NAVIGATION);
        articleMetadata.setArticle(article);
        given(articleRepository.save(article)).willReturn(article);

        // Mock FooterSection with a FooterItem that links back to the section and article
        FooterSection mockFooterSection = new FooterSection();
        FooterItem footerItem = new FooterItem();
        footerItem.setArticle(article);
        footerItem.setItemNumber(articleMetadata.getItemNumber());
        footerItem.setFooterSection(mockFooterSection);  // Set the FooterSection reference on the FooterItem
        mockFooterSection.setFooterItems(new ArrayList<>(List.of(footerItem)));  // Set mutable list of items

        // Mock the footer section retrieval to return the prepared section
        given(footerSectionService.get(articleMetadata.getSectionNumber())).willReturn(Optional.of(mockFooterSection));

        // When
        Article savedArticle = articleService.saveArticle(articleMetadata, null);

        // Then
        assertNotNull(savedArticle);
        assertEquals(ArticleType.NAVIGATION, savedArticle.getArticleType());
        then(navigationItemService).should().save(article, articleMetadata.getItemNumber());
        then(footerSectionService).should().get(articleMetadata.getSectionNumber());
        then(footerSectionService).should().save(mockFooterSection);  // Verify save is called on modified section
    }


    @Test
    void givenFooterArticleType_whenSaveArticle_thenHandlesFooterArticleTypeSpecifics() throws IOException {
        // Given
        article.setArticleType(ArticleType.FOOTER);
        articleMetadata.setArticle(article);
        given(articleRepository.save(article)).willReturn(article);

        // When
        Article savedArticle = articleService.saveArticle(articleMetadata, null);

        // Then
        assertNotNull(savedArticle);
        assertEquals(ArticleType.FOOTER, savedArticle.getArticleType());
        then(navigationItemService).should().deleteByArticle(article);
        then(footerSectionService).should().save(articleMetadata);
    }

    @Test
    void givenPromotionArticleType_whenSavePromotionArticle_thenHandlesPromotionWithLinkedProducts()
            throws IOException, PromotionNotFoundException {
        // Given
        article.setArticleType(ArticleType.PROMOTION);
        articleMetadata.setArticle(article);

        // Mock the `articleRepository.save` to return the article
        given(articleRepository.save(article)).willReturn(article);

        // Mock promotion retrieval to throw exception so a new one is created
        willThrow(new PromotionNotFoundException("Promotion not found")).given(promotionService).getByName(articleMetadata.getPromotionName());

        // Prepare a new Promotion and ensure it is linked with products
        Promotion newPromotion = new Promotion();
        newPromotion.setName(articleMetadata.getPromotionName());
        newPromotion.setProducts(List.of(new Product(), new Product()));

        // Mock promotionService.save to return the newPromotion after adding the article
        given(promotionService.save(any(Promotion.class))).willAnswer(invocation -> {
            Promotion promotion = invocation.getArgument(0);
            promotion.addArticle(article);
            return promotion;
        });

        // Mock findAllById for selected products in the promotion
        given(productRepository.findAllById(articleMetadata.getSelectedProducts())).willReturn(newPromotion.getProducts());

        // When
        Article savedArticle = articleService.savePromotionArticle(articleMetadata, null);

        // Then
        assertNotNull(savedArticle);
        assertEquals(ArticleType.PROMOTION, savedArticle.getArticleType());
        assertEquals(newPromotion.getName(), savedArticle.getPromotion().getName()); // Verify promotion association by name
        assertEquals(newPromotion.getProducts().size(), savedArticle.getPromotion().getProducts().size()); // Verify linked products

        then(promotionService).should().save(any(Promotion.class));
        then(productRepository).should(times(newPromotion.getProducts().size())).save(any(Product.class));
    }


    @Test
    void givenPromotionArticleMetadata_whenSavePromotionArticle_thenSavesPromotionAndLinksProducts()
            throws IOException, PromotionNotFoundException {
        // Given
        article.setArticleType(ArticleType.PROMOTION);
        articleMetadata.setArticle(article);

        // Initialize a Promotion with a non-null products list
        Promotion promotion = new Promotion();
        promotion.setName(articleMetadata.getPromotionName());
        promotion.setProducts(new ArrayList<>()); // Initialize products list

        // Configure mocks to return the initialized Promotion and a list of Products
        given(articleRepository.save(article)).willReturn(article);
        given(promotionService.getByName(articleMetadata.getPromotionName())).willReturn(promotion);

        // Mock finding products by ID to return a list of products
        List<Product> products = List.of(new Product(), new Product());
        given(productRepository.findAllById(articleMetadata.getSelectedProducts())).willReturn(products);

        // Mock the promotionService.save() to return the same promotion instance
        given(promotionService.save(promotion)).willReturn(promotion);

        // When
        Article savedArticle = articleService.savePromotionArticle(articleMetadata, null);

        // Then
        assertNotNull(savedArticle);
        assertEquals(article, savedArticle);
        assertEquals(promotion, savedArticle.getPromotion());
        assertNotNull(promotion.getProducts()); // Ensure promotion products are initialized
        assertEquals(2, promotion.getProducts().size()); // Ensure the products are linked correctly

        // Verify interactions with mocks
        then(promotionService).should().save(promotion);
        then(productRepository).should(times(products.size())).save(any(Product.class));
    }

    @Test
    void givenArticleWithoutNewImage_whenSave_thenSavesArticleDirectly() throws IOException {
        // Given
        given(articleRepository.save(article)).willReturn(article);

        // Use MockedStatic for FileService to verify no interactions with saveSingleFile
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            Article result = articleService.save(article, null);

            // Then
            assertNotNull(result);
            assertEquals(article, result);
            then(articleRepository).should().save(article);
            mockedFileService.verify(() -> FileService.saveSingleFile(any(), anyString(), anyString()), never());
        }
    }

    @Test
    void givenArticleWithNewImage_whenSave_thenSavesArticleWithImage() throws IOException {
        // Given
        given(newImage.getOriginalFilename()).willReturn("newImage.png");
        given(articleRepository.save(article)).willReturn(article);

        // Use MockedStatic to verify FileService.saveSingleFile was called with the correct arguments
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            Article result = articleService.save(article, newImage);

            // Then
            assertNotNull(result);
            assertEquals("newImage.png", result.getArticleImage());
            mockedFileService.verify(
                    () -> FileService.saveSingleFile(newImage, "article-photos/" + article.getId(), "newImage.png"));
        }
    }


    @Test
    void givenArticleTypeAndSort_whenFindAllByArticleType_thenReturnsSortedArticles() {
        // Given
        ArticleType type = ArticleType.PROMOTION;
        Sort.Direction direction = Sort.Direction.ASC;
        String sortField = "title";
        List<Article> articles = List.of(article);
        given(articleRepository.findAllByArticleType(type, Sort.by(direction, sortField))).willReturn(articles);

        // When
        List<Article> result = articleService.findAllByArticleType(type, sortField, direction);

        // Then
        assertNotNull(result);
        assertEquals(articles, result);
    }

    @Test
    void givenNullArticleType_whenFindAllByArticleType_thenReturnsEmptyList() {
        // Given
        given(articleRepository.findAllByArticleType(null, Sort.by(Sort.Direction.ASC, "title"))).willReturn(List.of());

        // When
        List<Article> result = articleService.findAllByArticleType(null, "title", Sort.Direction.ASC);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void givenUniqueTitle_whenIsArticleNameUnique_thenReturnsTrue() {
        // Given
        String title = "Unique Article";
        given(articleRepository.findByTitle(title)).willReturn(Optional.empty());

        // When
        boolean result = articleService.isArticleNameUnique(null, title);

        // Then
        assertTrue(result);
    }

    @Test
    void givenExistingArticleWithSameId_whenIsArticleNameUnique_thenReturnsTrue() {
        // Given
        String title = "Existing Article";
        article.setId(1);
        given(articleRepository.findByTitle(title)).willReturn(Optional.of(article));

        // When
        boolean result = articleService.isArticleNameUnique(1, title);

        // Then
        assertTrue(result);
    }

    @Test
    void givenDuplicateTitleWithDifferentId_whenIsArticleNameUnique_thenReturnsFalse() {
        // Given
        String title = "Duplicate Title";
        article.setId(1);
        Article otherArticle = new Article();
        otherArticle.setId(2);
        given(articleRepository.findByTitle(title)).willReturn(Optional.of(otherArticle));

        // When
        boolean result = articleService.isArticleNameUnique(1, title);

        // Then
        assertFalse(result);
    }


}