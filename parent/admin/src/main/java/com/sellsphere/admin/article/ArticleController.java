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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArticleController {

    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/articles/page/0?sortField=updatedTime&sortDir=asc";

    private final ArticleService articleService;
    private final UserService userService;
    private final NavigationItemService navigationItemService;
    private final FooterSectionService footerSectionService;
    private final FooterItemRepository footerItemRepository;
    private final PromotionService promotionService;

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
            throws ArticleNotFoundException, PromotionNotFoundException {

        Article article;
        String pageTitle;
        if (id == null) {
            article = new Article();
            pageTitle = "Create new Article";
        } else {
            article = articleService.get(id);
            pageTitle = "Update Article [" + id + "]";

            if (article.getArticleType().equals(ArticleType.NAVIGATION)) {
                NavigationItem navItem = navigationItemService.getByArticle(article);
                model.addAttribute("navItem", navItem);
            }

            if (article.getArticleType().equals(ArticleType.FOOTER)) {
                FooterItem footerItem = footerItemRepository.findByArticle(article).orElseThrow(
                        IllegalStateException::new);
                FooterSection footerSection = footerItem.getFooterSection();

                model.addAttribute("footerItem", footerItem);
                model.addAttribute("footerSection", footerSection);
            }

            if(article.getArticleType().equals(ArticleType.PROMOTION)) {
                Promotion promotion = promotionService.getByArticle(article);
                model.addAttribute("promotion", promotion);
            }

        }

        List<NavigationItem> navigationItemList = navigationItemService.findAll();
        // Create a list with nulls to ensure correct indexing
        List<NavigationItem> preparedNavigationItemList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            preparedNavigationItemList.add(null);
        }

        for (NavigationItem item : navigationItemList) {
            if (item.getItemNumber() >= 1 && item.getItemNumber() <= 5) {
                preparedNavigationItemList.set(item.getItemNumber() - 1, item);
            }
        }

        List<FooterSection> footerSectionList = footerSectionService.findAll();
        List<FooterSection> preparedFooterSectionList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            preparedFooterSectionList.add(null);
        }

        for (FooterSection section : footerSectionList) {
            if (section.getSectionNumber() >= 1 && section.getSectionNumber() <= 3) {
                preparedFooterSectionList.set(section.getSectionNumber() - 1, section);
                List<FooterItem> preparedFooterItemList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    preparedFooterItemList.add(null);
                }

                for (FooterItem footerItem : section.getFooterItems()) {
                    if (footerItem.getItemNumber() >= 1 && footerItem.getItemNumber() <= 5) {
                        preparedFooterItemList.set(footerItem.getItemNumber() - 1, footerItem);
                    }
                }

                section.setFooterItems(preparedFooterItemList);
            }
        }

        List<Promotion> promotionList = promotionService.listAll();

        model.addAttribute("promotionList", promotionList);
        model.addAttribute("footerSectionList", preparedFooterSectionList);
        model.addAttribute("navigationItemList", preparedNavigationItemList);
        model.addAttribute("article", article);
        model.addAttribute("pageTitle", pageTitle);

        return "article/article_form";
    }

    @PostMapping("/articles/save")
    public String saveArticle(@ModelAttribute("article") Article article,
                              @RequestParam(value = "newImage", required = false) MultipartFile newImage,
                              @RequestParam(value = "itemNumber", required = false) Integer itemNumber,
                              @RequestParam(value = "sectionNumber", required = false) Integer sectionNumber,
                              @RequestParam(value = "selectedProducts") List<Integer> promotionProducts,
                              @RequestParam(value = "promotionName") String promotionName,
                              Principal principal,
                              RedirectAttributes redirectAttributes)
            throws UserNotFoundException, IOException {
        // validate
        if (article.getArticleType().equals(ArticleType.NAVIGATION) && itemNumber == null) {
            throw new IllegalStateException("Article type navigation require item number");
        }

        if (article.getArticleType().equals(
                ArticleType.FOOTER) && (sectionNumber == null || itemNumber == null)) {
            throw new IllegalStateException(
                    "Article type footer require section number and item number");
        }

        if (article.getArticleType().equals(ArticleType.PROMOTION) &&
                (promotionProducts == null || promotionProducts.isEmpty() || promotionName == null)) {
            throw new IllegalStateException("Promotion article must have defined promotion that includes products and promotion name");
        }

        String successMessage = "Successfully " + (article.getId() != null ? "updated" : "created"
        ) + " article.";

        User user = getAuthenticatedUser(principal);

        Article savedArticle;
        switch (article.getArticleType()) {
            case PROMOTION:
                savedArticle = articleService.savePromotionArticle(article, newImage, user,
                                                                   promotionName,
                                                                   promotionProducts
                );
                break;
            default:
                savedArticle = articleService.save(article, newImage, user, itemNumber,
                                                   sectionNumber
                );
                break;
        }


        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL + "&keyword=" + savedArticle.getId();
    }

    private User getAuthenticatedUser(Principal principal) throws UserNotFoundException {
        return userService.get(principal.getName());
    }


}
