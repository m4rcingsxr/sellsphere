package com.sellsphere.client.article;

import com.sellsphere.common.entity.NavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NavigationItemRepository extends JpaRepository<NavigationItem, Integer> {

    List<NavigationItem> findByArticle_PublishedTrueOrderByItemNumberAsc();

}
