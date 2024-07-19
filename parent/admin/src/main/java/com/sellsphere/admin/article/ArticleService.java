package com.sellsphere.admin.article;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final int ARTICLE_PER_PAGE = 10;

    private final ArticleRepository articleRepository;

    public void listPage(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, ARTICLE_PER_PAGE, articleRepository);
    }

}
