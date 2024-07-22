package com.sellsphere.admin.article;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final String S3_FOLDER_NAME = "article-photos/";
    private static final int ARTICLE_PER_PAGE = 10;

    private final ArticleRepository articleRepository;
    private final NavigationItemService navigationItemService;
    private final FooterSectionService footerSectionService;
    private final PromotionService promotionService;
    private final ProductRepository productRepository;
    private final FooterItemRepository footerItemRepository;

    public void listPage(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, ARTICLE_PER_PAGE, articleRepository);
    }

    @Transactional
    public Article saveArticle(Article article,
                               MultipartFile newImage,
                               User user,
                               Integer itemNumber,
                               Integer sectionNumber,
                               String promotionName,
                               List<Integer> promotionProducts) throws IOException {

        Article savedArticle;
        if (article.getArticleType() == ArticleType.PROMOTION) {
            savedArticle = savePromotionArticle(article, newImage, user, promotionName, promotionProducts);
        } else {
            savedArticle = save(article, newImage, user);
        }

        handleArticleTypeSpecifics(savedArticle, itemNumber, sectionNumber);
        return savedArticle;
    }

    private void handleArticleTypeSpecifics(Article article, Integer itemNumber, Integer sectionNumber) {
        switch (article.getArticleType()) {
            case NAVIGATION -> {
                promotionService.delete(article);
                footerItemRepository.deleteByArticle(article);
                navigationItemService.save(article, itemNumber);
            }
            case FOOTER -> {
                promotionService.delete(article);
                footerSectionService.save(article, itemNumber, sectionNumber);
                navigationItemService.deleteByArticle(article);
            }
            case PROMOTION -> {
                footerItemRepository.deleteByArticle(article);
                navigationItemService.deleteByArticle(article);
            }
            case FREE -> {
                promotionService.delete(article);
                footerItemRepository.deleteByArticle(article);
                navigationItemService.deleteByArticle(article);
            }
        }
    }

    @Transactional
    public Article savePromotionArticle(Article article,
                                        MultipartFile newImage,
                                        User user,
                                        String promotionName,
                                        List<Integer> promotionProducts) throws IOException {

        Article savedArticle = save(article, newImage, user);
        Promotion promotion;

        unlinkExistingPromotion(article);

        try {
            promotion = promotionService.get(promotionName);

            if (!promotion.getArticle().getId().equals(article.getId())) {
                unlinkExistingPromotion(article);
            }
        } catch (PromotionNotFoundException e) {
            promotion = new Promotion();
        }

        promotion.setArticle(savedArticle);
        promotion.setName(promotionName);
        promotion.setProducts(productRepository.findAllById(promotionProducts));
        promotionService.save(promotion);

        return savedArticle;
    }

    private void unlinkExistingPromotion(Article article) {
        try {
            Promotion existingPromotion = promotionService.getByArticle(article);
            promotionService.delete(existingPromotion);
        } catch (PromotionNotFoundException ex) {
            // no need for unlink, promo does not exist.
        }
    }

    @Transactional
    public Article save(Article article, MultipartFile newImage, User user) throws IOException {
        setCommonArticleProperties(article, user);

        if (newImage != null && !newImage.isEmpty()) {
            article.setArticleImage(newImage.getOriginalFilename());
            Article savedArticle = articleRepository.save(article);

            String fileName = newImage.getOriginalFilename();
            String folderName = S3_FOLDER_NAME + savedArticle.getId();

            FileService.saveSingleFile(newImage, folderName, fileName);
            return savedArticle;
        } else {
            return articleRepository.save(article);
        }
    }

    private void setCommonArticleProperties(Article article, User user) {
        article.setUpdatedTime(LocalDate.now());
        if (article.getCreatedBy() == null) {
            article.setCreatedBy(user);
        }
        if (article.getAlias() == null) {
            article.setAlias(article.getTitle().toLowerCase().replaceAll("\\s+", "-"));
        }
    }

    public Article get(Integer id) throws ArticleNotFoundException {
        return articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new);
    }
}
