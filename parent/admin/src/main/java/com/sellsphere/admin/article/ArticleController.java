package com.sellsphere.admin.article;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/articles/page/0?sortField=updatedTime&sortDir=asc";

    @GetMapping("/articles")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/articles/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "articleList", moduleURL =
                    "/articles") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {

        articleService.listPage(helper, pageNum);

        return "article/articles";
    }

    @GetMapping({"/articles/new", "/articles/edit/{id}"})
    public String showArticleForm(@PathVariable(required = false) Integer id,
                                  Model model)
            throws ArticleNotFoundException {

        Article article;
        String pageTitle;
        if(id == null) {
            article = new Article();
            pageTitle = "Create new Article";
        } else {
            article = null;
            pageTitle = "Update Article [" + id + "]";
        }

        model.addAttribute("article", article);
        model.addAttribute("pageTitle", pageTitle);

        return "article/article_form";
    }



}
