package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleNotFoundException;
import com.sellsphere.common.entity.ArticleType;
import com.sellsphere.common.entity.NavigationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NavigationItemService {

    private final NavigationItemRepository navigationItemRepository;

    public NavigationItem save(Article article, Integer itemNumber) {
        Optional<NavigationItem> navItemOpt = navigationItemRepository.findByArticle(article);
        if(navItemOpt.isPresent()) {
            NavigationItem navigationItem = navItemOpt.get();

            if(navigationItem.getItemNumber().equals(itemNumber)) {
                return navigationItem;
            } else {

                navigationItem.setItemNumber(itemNumber);
                return navigationItemRepository.save(navigationItem);
            }
        } else {
            article.setPublished(true);

            NavigationItem navigationItem = new NavigationItem();
            navigationItem.setItemNumber(itemNumber);
            navigationItem.setArticle(article);
            return navigationItemRepository.save(navigationItem);
        }
    }

    public List<NavigationItem> findAll() {
        return navigationItemRepository.findAll(Sort.by(Sort.Direction.ASC, "itemNumber"));
    }

    public NavigationItem getByArticle(Article article) throws ArticleNotFoundException {
        return navigationItemRepository.findByArticle(article).orElseThrow(
                ArticleNotFoundException::new);
    }

    public void deleteByArticle(Article savedArticle) {
        navigationItemRepository.deleteByArticle(savedArticle);
    }
}
