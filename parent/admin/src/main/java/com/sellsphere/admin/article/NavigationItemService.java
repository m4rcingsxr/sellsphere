package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleNotFoundException;
import com.sellsphere.common.entity.NavigationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NavigationItemService {

    private final NavigationItemRepository navigationItemRepository;

    /**
     * Saves or updates the NavigationItem associated with the given article.
     * If the article already exists, the item number is updated only if necessary.
     *
     * @param article the article to associate with a NavigationItem
     * @param itemNumber the item number to set
     * @return the saved or updated NavigationItem
     */
    public NavigationItem save(Article article, Integer itemNumber) {
        return navigationItemRepository.findByArticle(article)
                .map(existingNavItem -> updateNavigationItemIfNecessary(existingNavItem, itemNumber))
                .orElseGet(() -> createNewNavigationItem(article, itemNumber));
    }

    private NavigationItem updateNavigationItemIfNecessary(NavigationItem navItem, Integer itemNumber) {
        if (!navItem.getItemNumber().equals(itemNumber)) {
            navItem.setItemNumber(itemNumber);
            return navigationItemRepository.save(navItem);
        }
        return navItem;
    }

    private NavigationItem createNewNavigationItem(Article article, Integer itemNumber) {
        article.setPublished(true);
        NavigationItem navItem = new NavigationItem();
        navItem.setArticle(article);
        navItem.setItemNumber(itemNumber);
        return navigationItemRepository.save(navItem);
    }

    /**
     * Retrieves all NavigationItems sorted by itemNumber in ascending order.
     *
     * @return a sorted list of NavigationItems
     */
    public List<NavigationItem> findAll() {
        return navigationItemRepository.findAll(Sort.by(Sort.Direction.ASC, "itemNumber"));
    }

    /**
     * Retrieves a NavigationItem by the associated article, or throws an exception if not found.
     *
     * @param article the article associated with the NavigationItem
     * @return the NavigationItem found
     * @throws ArticleNotFoundException if no NavigationItem is associated with the article
     */
    public NavigationItem getByArticle(Article article) throws ArticleNotFoundException {
        return navigationItemRepository.findByArticle(article)
                .orElseThrow(() -> new ArticleNotFoundException("Navigation item not found for article id: " + article.getId()));
    }

    /**
     * Deletes the NavigationItem associated with the given article.
     *
     * @param article the article whose NavigationItem is to be deleted
     */
    public void deleteByArticle(Article article) {
        navigationItemRepository.deleteByArticle(article);
    }
}
