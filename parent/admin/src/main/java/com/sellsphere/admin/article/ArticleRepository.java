package com.sellsphere.admin.article;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends SearchRepository<Article, Integer> {

    @Override
    @Query("SELECT a FROM Article a WHERE LOWER(CONCAT(a.id, ' ', a.content, ' ', a.createdBy.firstName, ' ', a" +
            ".createdBy.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Article> findAll(@Param("keyword") String keyword, Pageable pageable);

    List<Article> findAllByArticleType(ArticleType type, Sort sort);

    List<Article> findAllByTitleIn(List<String> titles);

    Optional<Article> findByTitle(String title);
}
