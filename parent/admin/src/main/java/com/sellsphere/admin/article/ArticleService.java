package com.sellsphere.admin.article;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.ArticleMetadata;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

    private static final String S3_FOLDER_NAME = "article-photos/";
    private static final int ARTICLES_PER_PAGE = 10;

    private final ArticleRepository articleRepository;
    private final NavigationItemService navigationItemService;
    private final FooterSectionService footerSectionService;
    private final PromotionService promotionService;
    private final ProductRepository productRepository;

    /**
     * Lists articles for a given page using pagination.
     *
     * @param helper  Paging and sorting helper
     * @param pageNum The page number to retrieve
     */
    public void listPage(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, ARTICLES_PER_PAGE, articleRepository);
    }

    /**
     * Saves an article and handles article type-specific logic.
     *
     * @param metadata Metadata containing article information
     * @param newImage The new image file, if available
     * @return The saved article
     * @throws IOException If an error occurs while processing the image
     */
    public Article saveArticle(ArticleMetadata metadata, MultipartFile newImage) throws IOException {
        setCommonArticleProperties(metadata.getArticle(), metadata.getUser());

        Article savedArticle;
        if (metadata.getArticle().getArticleType() == ArticleType.PROMOTION) {
            savedArticle = savePromotionArticle(metadata, newImage);
        } else {
            savedArticle = save(metadata.getArticle(), newImage);
        }

        handleArticleTypeSpecifics(metadata);
        return savedArticle;
    }

    /**
     * Saves a promotion article and links it with the relevant promotion details.
     *
     * @param metadata Metadata containing promotion details
     * @param newImage The new image file, if available
     * @return The saved article
     * @throws IOException If an error occurs while processing the image
     */
    public Article savePromotionArticle(ArticleMetadata metadata, MultipartFile newImage) throws IOException {
        Article savedArticle = save(metadata.getArticle(), newImage);
        Promotion promotion = getOrCreatePromotion(metadata.getPromotionName());

        promotion.addArticle(savedArticle);
        promotion.setProducts(productRepository.findAllById(metadata.getSelectedProducts()));
        Promotion savedPromotion = promotionService.save(promotion);

        linkPromotionToProductDetails(savedPromotion);
        return savedArticle;
    }

    /**
     * Saves an article and its associated image to the repository.
     *
     * @param article  The article to save
     * @param newImage The new image file, if available
     * @return The saved article
     * @throws IOException If an error occurs while saving the image
     */
    public Article save(Article article, MultipartFile newImage) throws IOException {
        unlinkIfNeeded(article);

        if (newImage != null && !newImage.isEmpty()) {
            return saveArticleWithImage(article, newImage);
        }
        return articleRepository.save(article);
    }

    /**
     * Sets common properties for an article (e.g., updated time, created by, and alias).
     *
     * @param article The article to update
     * @param user    The user associated with the article
     */
    private void setCommonArticleProperties(Article article, User user) {
        article.setUpdatedTime(LocalDate.now());
        article.setCreatedBy(Optional.ofNullable(article.getCreatedBy()).orElse(user));
        article.setAlias(Optional.ofNullable(article.getAlias()).orElse(generateAliasFromTitle(article.getTitle())));
    }

    /**
     * Generates an alias for the article title by converting it to lowercase and replacing spaces with hyphens.
     *
     * @param title The article title
     * @return The generated alias
     */
    private String generateAliasFromTitle(String title) {
        return title.toLowerCase().replace(" ", "-");
    }

    /**
     * Handles logic specific to the type of article (e.g., navigation, footer, promotion, free).
     *
     * @param metadata The metadata containing type-specific information
     */
    private void handleArticleTypeSpecifics(ArticleMetadata metadata) {
        Article article = metadata.getArticle();
        switch (article.getArticleType()) {
            case NAVIGATION -> handleNavigationArticle(article, metadata.getItemNumber(), metadata.getSectionNumber());
            case FOOTER -> handleFooterArticle(metadata);
            case PROMOTION -> handlePromotionArticle(article, metadata.getSectionNumber());
            case FREE -> handleFreeArticle(article, metadata.getSectionNumber());
        }
    }

    private void handleNavigationArticle(Article article, Integer itemNumber, Integer sectionNumber) {
        navigationItemService.save(article, itemNumber);
        removeFooterItemByArticle(article, sectionNumber);
    }

    private void handleFooterArticle(ArticleMetadata metadata) {
        navigationItemService.deleteByArticle(metadata.getArticle());
        footerSectionService.save(metadata);
    }

    private void handlePromotionArticle(Article article, Integer sectionNumber) {
        removeFooterItemByArticle(article, sectionNumber);
    }

    private void handleFreeArticle(Article article, Integer sectionNumber) {
        navigationItemService.deleteByArticle(article);
        removeFooterItemByArticle(article, sectionNumber);
    }

    private void removeFooterItemByArticle(Article article, Integer sectionNumber) {
        if (sectionNumber == null) return;

        footerSectionService.get(sectionNumber)
                .map(FooterSection::getFooterItems)
                .flatMap(footerItems -> footerItems.stream()
                        .filter(item -> item.getArticle().getId().equals(article.getId()))
                        .findFirst())
                .ifPresent(footerItem -> {
                    footerItem.getFooterSection().getFooterItems().remove(footerItem);
                    footerSectionService.save(footerItem.getFooterSection());
                });
    }

    private Promotion getOrCreatePromotion(String promotionName) {
        Promotion promotion;
        try {
            promotion = promotionService.getByName(promotionName);
        } catch (PromotionNotFoundException e) {
            promotion = new Promotion();
        }
        promotion.setName(promotionName);
        return promotion;
    }

    private void linkPromotionToProductDetails(Promotion promotion) {
        promotion.getProducts().forEach(product -> {
            boolean detailExists = product.getDetails().stream()
                    .anyMatch(detail -> promotion.getName().equals(detail.getValue()));

            if (!detailExists) {
                ProductDetail productDetail = new ProductDetail("Promotion", promotion.getName(), product);
                product.addProductDetail(productDetail);
                productRepository.save(product);
            }
        });
    }

    private void unlinkExistingPromotionDetails(Promotion promotion) {
        promotion.getProducts().forEach(
                product -> product.getDetails().removeIf(detail -> detail.getValue().equals(promotion.getName())));
        productRepository.saveAll(promotion.getProducts());
    }

    private void unlinkIfNeeded(Article article) {
        if (article.getId() != null) {
            articleRepository.findById(article.getId())
                    .filter(existingArticle -> existingArticle.getArticleType() == ArticleType.PROMOTION)
                    .ifPresent(existingArticle -> unlinkExistingPromotionDetails(existingArticle.getPromotion()));
        }
    }

    private Article saveArticleWithImage(Article article, MultipartFile newImage) throws IOException {
        article.setArticleImage(newImage.getOriginalFilename());
        Article savedArticle = articleRepository.save(article);

        String fileName = newImage.getOriginalFilename();
        String folderName = S3_FOLDER_NAME + savedArticle.getId();

        FileService.saveSingleFile(newImage, folderName, fileName);
        return savedArticle;
    }

    public Article get(Integer id) throws ArticleNotFoundException {
        return articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new);
    }

    public void deleteArticle(Integer articleId) throws ArticleNotFoundException {
        Article article = get(articleId);
        if (article.getArticleType() == ArticleType.PROMOTION) {
            unlinkExistingPromotionDetails(article.getPromotion());
        }

        S3Utility.removeFolder(S3_FOLDER_NAME + article.getId());
        articleRepository.delete(article);
    }

    public List<Article> findAllByArticleType(ArticleType articleType, String sortField, Sort.Direction direction) {
        return articleRepository.findAllByArticleType(articleType, Sort.by(direction, sortField));
    }

    public List<Article> findAllByTitles(List<String> titles) {
        return articleRepository.findAllByTitleIn(titles);
    }

    public boolean isArticleNameUnique(Integer id, String title) {
        return articleRepository.findByTitle(title)
                .map(existingArticle -> existingArticle.getId().equals(id))
                .orElse(true);
    }

    public List<Article> listAll(String field, Sort.Direction direction) {
        return articleRepository.findAll(Sort.by(direction, field));
    }
}
