package com.sellsphere.admin.article;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Article;
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

    public void listPage(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, ARTICLE_PER_PAGE, articleRepository);
    }

    @Transactional
    public Article save(Article article, MultipartFile newImage, User user) throws IOException {

        article.setUpdatedTime(LocalDate.now());

        if(article.getCreatedBy() == null) {
            article.setCreatedBy(user);
        }

        if(article.getAlias() == null) {
            article.setAlias(article.getTitle().toLowerCase());
        }


        if(newImage != null && !newImage.isEmpty()) {
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
}
