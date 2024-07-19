package com.sellsphere.admin.article;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.admin.user.UserService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/articles/page/0?sortField=updatedTime&sortDir=asc";
    private final UserService userService;

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
    public String showArticleForm(@PathVariable(required = false) Integer id, Model model)
            throws ArticleNotFoundException {

        Article article;
        String pageTitle;
        if (id == null) {
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

    @PostMapping("/articles/save")
    public String saveArticle(@ModelAttribute("article") Article article,
                              @RequestParam(value = "newImage", required = false) MultipartFile newImage,
                              Principal principal,
                              RedirectAttributes redirectAttributes)
            throws UserNotFoundException, IOException {
        String successMessage = "Successfully " + (article.getId() != null ? "updated" : "created"
        ) + " article.";

        User user = getAuthenticatedUser(principal);

        Article savedArticle = articleService.save(article, newImage, user);

        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL + "&keyword=" + savedArticle.getId();
    }

    private User getAuthenticatedUser(Principal principal) throws UserNotFoundException {
        return userService.get(principal.getName());
    }


}
