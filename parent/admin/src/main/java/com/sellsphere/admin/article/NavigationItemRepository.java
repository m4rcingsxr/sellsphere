package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.NavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NavigationItemRepository extends JpaRepository<NavigationItem, Integer> {

    Optional<NavigationItem> findByArticle(Article article);

    void deleteByArticle(Article article);
}
