package com.sellsphere.admin.article;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.admin.user.UserService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.ArticleMetadata;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
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
    private final PromotionService promotionService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class,
                                    new StringTrimmerEditor(true)
        );
    }

    // Redirects to the first page of articles with default sorting
    @GetMapping("/articles")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    // Lists articles with pagination and sorting
    @GetMapping("/articles/page/{pageNum}")
    public String listPage(@PagingAndSortingParam(listName = "articleList", moduleURL = "/articles") PagingAndSortingHelper helper,
                           @PathVariable("pageNum") Integer pageNum) {
        articleService.listPage(helper, pageNum);
        return "article/articles";
    }

    /**
     * Displays the form to create or edit an article.
     * @param id The article ID (nullable). If null, it is a new article.
     * @param model Model object to pass data to the view.
     * @return The article form view.
     * @throws ArticleNotFoundException if the article is not found.
     * @throws PromotionNotFoundException if the promotion is not found for promotion articles.
     */
    @GetMapping({"/articles/new", "/articles/edit/{id}"})
    public String showArticleForm(@PathVariable(required = false) Integer id, Model model)
            throws ArticleNotFoundException, PromotionNotFoundException {

        ArticleMetadata metadata;
        String pageTitle;


        // New Article
        if (id == null) {
            metadata = ArticleMetadata.builder().article(new Article()).build();
            pageTitle = "Create new Article";
        }
        // Editing Existing Article
        else {
            Article article = articleService.get(id);
            metadata = ArticleMetadata.builder().article(article).build();
            pageTitle = "Update Article [" + id + "]";
            populateArticleSpecificAttributes(model, article);  // Adds article-specific data to the model
        }

        populateCommonAttributes(model, metadata, pageTitle);

        return "article/article_form";
    }

    /**
     * Populates model with specific attributes for article types like NAVIGATION, FOOTER, or PROMOTION.
     * This method handles data specific to each article type and adds it to the model.
     *
     * @param model   The model to add attributes to.
     * @param article The article object used to extract data.
     * @throws ArticleNotFoundException      If the article is not found.
     * @throws PromotionNotFoundException    If the promotion associated with an article is not found.
     * @throws IllegalStateException         If article type is not recognized by method
     */
    private void populateArticleSpecificAttributes(Model model, Article article)
            throws ArticleNotFoundException, PromotionNotFoundException {

        switch (article.getArticleType()) {
            case NAVIGATION -> {
                NavigationItem navItem = navigationItemService.getByArticle(article);
                model.addAttribute("navItem", navItem);
            }

            case FOOTER -> {
                FooterItem footerItem = footerSectionService.getFooterItemByArticle(article);
                FooterSection footerSection = footerItem.getFooterSection();
                model.addAttribute("footerItem", footerItem);
                model.addAttribute("footerSection", footerSection);
            }

            case PROMOTION -> {
                Promotion promotion = promotionService.getByArticle(article);
                model.addAttribute("promotion", promotion);
            }
        }
    }


    private void populateCommonAttributes(Model model, ArticleMetadata metadata, String pageTitle) {
        model.addAttribute("navigationItemList", prepareNavigationItems());
        model.addAttribute("footerSectionList", prepareFooterSections());
        model.addAttribute("promotionList", promotionService.listAll());
        model.addAttribute("articleMetadata", metadata);
        model.addAttribute("pageTitle", pageTitle);
    }

    /**
     * Prepares a list of navigation items with null values filling the missing items up to 5.
     *
     * @return A prepared list of NavigationItem objects.
     */
    private List<NavigationItem> prepareNavigationItems() {
        return ListUtil.prepareListWithNulls(navigationItemService.findAll(), 5, NavigationItem::getItemNumber);
    }

    /**
     * Prepares a list of footer sections, each filled with footer items (up to 5) or nulls if missing.
     *
     * @return A prepared list of FooterSection objects with up to 5 footer items.
     */
    private List<FooterSection> prepareFooterSections() {
        return ListUtil.prepareListWithNulls(footerSectionService.findAll(), 3, FooterSection::getSectionNumber)
                .stream()
                .peek(this::prepareFooterItems)
                .toList();  // Java 17 toList() to collect the result as a list.
    }

    /**
     * Prepares the footer items for each section, filling with null values if necessary.
     *
     * @param section The footer section to prepare.
     */
    private void prepareFooterItems(FooterSection section) {
        if (section != null) {
            List<FooterItem> preparedFooterItemList = ListUtil.prepareListWithNulls(section.getFooterItems(), 5, FooterItem::getItemNumber);
            section.setFooterItems(preparedFooterItemList);
        }
    }

    @PostMapping("/articles/save")
    public String saveArticle(
            @RequestParam(value = "newImage", required = false) MultipartFile newImage,
            @ModelAttribute @Valid ArticleMetadata metadata,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) throws UserNotFoundException, IOException {

        ValidationHelper validationHelper = new ValidationHelper(result, "error.article");
        validationHelper.validateMultipartFile(newImage, metadata.getArticle() != null ? metadata.getArticle().getId() : null, "article.articleImage", "A main article image is required.");

        if (result.hasErrors()) {
            populateCommonAttributes(model, metadata, "Create or Update Article");
            return "article/article_form";
        }

        String successMessage = "Successfully " +
                (metadata.getArticle().getId() != null ? "updated" : "created") +
                " article.";

        metadata.setUser(getAuthenticatedUser(principal));

        Article savedArticle = articleService.saveArticle(metadata, newImage);

        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL + "&keyword=" + savedArticle.getId();
    }

    @GetMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable("id") Integer articleId, RedirectAttributes redirectAttributes)
            throws ArticleNotFoundException {
        articleService.deleteArticle(articleId);

        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Successfully removed article");
        return DEFAULT_REDIRECT_URL;
    }


    /**
     * Retrieves the authenticated user from the principal object.
     *
     * @param principal The currently authenticated user principal.
     * @return The authenticated User object.
     * @throws UserNotFoundException If the user is not found in the system.
     */
    private User getAuthenticatedUser(Principal principal) throws UserNotFoundException {
        return userService.get(principal.getName());
    }

}
