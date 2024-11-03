package com.sellsphere.client.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    Optional<Article> findByAlias(String alias);

    List<Article> findAllByArticleTypeAndPublishedIsTrue(ArticleType type);

    List<Article> findAllByPublishedIsTrue(Sort articleType);
}
