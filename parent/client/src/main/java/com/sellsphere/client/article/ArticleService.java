package com.sellsphere.client.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article get(String alias) throws ArticleNotFoundException {
        return articleRepository.findByAlias(alias).orElseThrow(ArticleNotFoundException::new);
    }

    public List<Article> listAllByArticleType() {
        return articleRepository.findAllByPublishedIsTrue(Sort.by("articleType"));
    }

    public List<Article> listAllByArticleType(ArticleType articleType) {
        return articleRepository.findAllByArticleTypeAndPublishedIsTrue(articleType);
    }
}
