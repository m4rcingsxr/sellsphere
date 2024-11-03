package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.sellsphere.common.entity.Article}
 */
@Value
public class ArticleDTO implements Serializable {
    Integer id;
    String title;
    String alias;
    String content;
    LocalDate updatedTime;
    Boolean published;
    ArticleType articleType;
    String mainImagePath;

    public ArticleDTO(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.alias = article.getAlias();
        this.content = article.getContent();
        this.updatedTime = article.getUpdatedTime();
        this.published = article.getPublished();
        this.articleType = article.getArticleType();
        this.mainImagePath = article.getMainImagePath();
    }
}