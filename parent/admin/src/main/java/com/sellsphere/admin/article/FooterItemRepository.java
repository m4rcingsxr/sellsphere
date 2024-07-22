package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.FooterItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FooterItemRepository extends JpaRepository<FooterItem, Integer> {
    Optional<FooterItem> findByArticle(Article article);

    Optional<FooterItem> findByItemNumber(Integer itemNumber);

    void deleteByArticle(Article savedArticle);
}
