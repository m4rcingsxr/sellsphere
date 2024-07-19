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
    private final PromotionRepository promotionRepository;

    public void listPage(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, ARTICLE_PER_PAGE, articleRepository);
    }

    @Transactional
    public Article save(Article article,
                        MultipartFile newImage,
                        User user,
                        Integer itemNumber,
                        Integer sectionNumber
    ) throws IOException {

        article.setUpdatedTime(LocalDate.now());

        if(article.getCreatedBy() == null) {
            article.setCreatedBy(user);
        }

        if(article.getAlias() == null) {
            article.setAlias(article.getTitle().toLowerCase());
        }

        Article savedArticle = save(article, newImage, user);

        switch (savedArticle.getArticleType()) {
            case NAVIGATION -> navigationItemService.save(savedArticle, itemNumber);
            case FOOTER -> footerSectionService.save(savedArticle, itemNumber, sectionNumber);
            case FREE -> {
                navigationItemService.deleteByArticle(savedArticle);
            }
        }

        return savedArticle;
    }

    public Article get(Integer id) throws ArticleNotFoundException {
        return articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new);
    }

    @Transactional
    public Article savePromotionArticle(Article article,
                                        MultipartFile newImage,
                                        User user,
                                        String promotionName,
                                        List<Integer> promotionProducts)
            throws IOException {
        Article savedArticle = save(article, newImage, user);

        try {
            Promotion promotion = promotionService.get(promotionName);

            if(!promotion.getArticle().getId().equals(article.getId())) {
                promotion.getArticle().setArticleType(ArticleType.FREE);
                articleRepository.saveAndFlush(article);
            }

            // check if saved article currently is not assigned to promotion

            try {
                Promotion existingPromotion = promotionService.getByArticle(savedArticle);
                if(existingPromotion.getArticle().getId().equals(article.getId())) {
                    // current article is assigned to the new promotion from previous promotion - delete promotion
                    promotionService.delete(existingPromotion);
                    promotionRepository.flush();
                }
            } catch(PromotionNotFoundException ex) {
                // ignore
            }


            promotion.setArticle(savedArticle);
            promotion.setProducts(productRepository.findAllById(promotionProducts));
            promotion.setName(promotionName);
            promotionService.save(promotion);
        } catch (PromotionNotFoundException e) {
            Promotion promotion = new Promotion();
            promotion.setArticle(savedArticle);
            promotion.setName(promotionName);
            promotion.setProducts(productRepository.findAllById(promotionProducts));
            promotionService.save(promotion);
        }

        return savedArticle;
    }

    private Article save(Article article, MultipartFile newImage, User user) throws IOException {
        article.setUpdatedTime(LocalDate.now());

        if(article.getCreatedBy() == null) {
            article.setCreatedBy(user);
        }

        if(article.getAlias() == null) {
            article.setAlias(article.getTitle().toLowerCase());
        }

        Article savedArticle;
        if(newImage != null && !newImage.isEmpty()) {
            article.setArticleImage(newImage.getOriginalFilename());
            savedArticle = articleRepository.save(article);

            String fileName = newImage.getOriginalFilename();

            String folderName = S3_FOLDER_NAME + savedArticle.getId();

            FileService.saveSingleFile(newImage, folderName, fileName);
            return savedArticle;
        } else {
            return articleRepository.save(article);
        }
    }

}
