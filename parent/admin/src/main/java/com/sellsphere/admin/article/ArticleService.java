package com.sellsphere.admin.article;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleNotFoundException;
import com.sellsphere.common.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final String S3_FOLDER_NAME = "article-photos/";
    private static final int ARTICLE_PER_PAGE = 10;

    private final ArticleRepository articleRepository;
    private final NavigationItemService navigationItemService;
    private final FooterSectionService footerSectionService;

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

        Article savedArticle;
        if(newImage != null && !newImage.isEmpty()) {
            article.setArticleImage(newImage.getOriginalFilename());
            savedArticle = articleRepository.save(article);

            String fileName = newImage.getOriginalFilename();

            String folderName = S3_FOLDER_NAME + savedArticle.getId();

            FileService.saveSingleFile(newImage, folderName, fileName);
        } else {
            savedArticle = articleRepository.save(article);
        }

        switch (savedArticle.getArticleType()) {
            case NAVIGATION -> navigationItemService.save(savedArticle, itemNumber);
            case FOOTER -> footerSectionService.save(savedArticle, itemNumber, sectionNumber);
            case FREE -> {
                // check for association with ui elements
                navigationItemService.deleteByArticle(savedArticle);
            }
        }

        return savedArticle;
    }

    public Article get(Integer id) throws ArticleNotFoundException {
        return articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new);
    }
}
