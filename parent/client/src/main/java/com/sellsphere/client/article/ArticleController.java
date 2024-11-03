package com.sellsphere.client.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles/{alias}")
    public String showArticle(@PathVariable String alias, Model model) throws ArticleNotFoundException {
        Article article = articleService.get(alias);
        model.addAttribute("article", article);

        List<Article> articleList = articleService.listAllByArticleType();
        List<Article> promotionArticleList = articleService.listAllByArticleType(ArticleType.PROMOTION);
        List<Article> helpArticleList = articleService.listAllByArticleType(ArticleType.FOOTER);
        model.addAttribute("articleList", articleList);
        model.addAttribute("promotionArticleList", promotionArticleList);
        model.addAttribute("helpArticleList", helpArticleList);

        return "article/article";
    }

}
