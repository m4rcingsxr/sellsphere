package com.sellsphere.admin;

import com.sellsphere.admin.article.ArticleService;
import com.sellsphere.admin.brand.BrandService;
import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.admin.customer.CustomerService;
import com.sellsphere.admin.mainpage.CarouselService;
import com.sellsphere.admin.order.OrderService;
import com.sellsphere.admin.product.ProductService;
import com.sellsphere.admin.question.QuestionService;
import com.sellsphere.admin.review.ReviewService;
import com.sellsphere.admin.setting.CountryRepository;
import com.sellsphere.admin.setting.SettingService;
import com.sellsphere.admin.setting.StateRepository;
import com.sellsphere.admin.user.UserService;
import com.sellsphere.common.entity.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final SettingService settingService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;
    private final QuestionService questionService;
    private final ReviewService reviewService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final ArticleService articleService;
    private final CarouselService carouselService;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/")
    public String showMainPage(RedirectAttributes ra, HttpServletRequest request, Principal principal, Model model) {
        String continueParam = request.getParameter("continue");

        if (continueParam != null) {
            ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Welcome " + principal.getName());
            return "redirect:/";
        }

        loadModelAttributes(model);
        return "index";
    }

    private void loadModelAttributes(Model model) {
        loadSettingsToModel(settingService.listGeneralSettings(), model);
        loadSettingsToModel(settingService.listMailServerSettings(), model);
        loadSettingsToModel(settingService.getCurrencySettings(), model);

        model.addAttribute("userCount", populateUserAttributes(model));
        model.addAttribute("categoryCount", populateCategoryAttributes(model));
        model.addAttribute("brandCount", brandService.listAllBrands("name", Sort.Direction.ASC).size());
        model.addAttribute("productCount", populateProductAttributes(model));
        model.addAttribute("questionCount", populateQuestionAttributes(model));
        model.addAttribute("reviewCount", populateReviewAttributes(model));
        model.addAttribute("customerCount", populateCustomerAttributes(model));
        model.addAttribute("orderCount", populateOrderAttributes(model));
        model.addAttribute("articleCount", populateArticleAttributes(model));
        model.addAttribute("carouselCount", populateCarouselAttributes(model));
        model.addAttribute("countryCount", countryRepository.count());
        model.addAttribute("stateCount", stateRepository.count());
    }

    private long populateUserAttributes(Model model) {
        List<User> users = userService.listAll("firstName", Sort.Direction.ASC);
        long enabledUserCount = users.stream().filter(User::isEnabled).count();
        model.addAttribute("enabledUserCount", enabledUserCount);
        return users.size();
    }

    private long populateCategoryAttributes(Model model) {
        List<Category> categories = categoryService.listAll("name", Sort.Direction.ASC);
        long enabledCategoryCount = categories.stream().filter(Category::isEnabled).count();
        model.addAttribute("enabledCategoryCount", enabledCategoryCount);
        return categories.size();
    }

    private long populateProductAttributes(Model model) {
        List<Product> products = productService.listAllProducts("name", Sort.Direction.ASC);
        long enabledProductCount = products.stream().filter(Product::isEnabled).count();
        long inStockProductCount = products.stream().filter(Product::isInStock).count();
        model.addAttribute("enabledProductCount", enabledProductCount);
        model.addAttribute("inStockProductCount", inStockProductCount);
        return products.size();
    }

    private long populateQuestionAttributes(Model model) {
        List<Question> questions = questionService.listAll("askTime", Sort.Direction.ASC);
        long answeredQuestionCount = questions.stream().filter(question -> question.getAnswerContent() != null).count();
        long approvedQuestionCount = questions.stream().filter(Question::getApprovalStatus).count();
        model.addAttribute("answeredQuestionCount", answeredQuestionCount);
        model.addAttribute("approvedQuestionCount", approvedQuestionCount);
        return questions.size();
    }

    private long populateReviewAttributes(Model model) {
        List<Review> reviews = reviewService.listAll("reviewTime", Sort.Direction.ASC);
        long approvedReviewCount = reviews.stream().filter(Review::getApproved).count();
        model.addAttribute("approvedReviewCount", approvedReviewCount);
        return reviews.size();
    }

    private long populateCustomerAttributes(Model model) {
        List<Customer> customers = customerService.listAll("firstName", Sort.Direction.ASC);
        long enabledCustomerCount = customers.stream().filter(Customer::isEnabled).count();
        model.addAttribute("enabledCustomerCount", enabledCustomerCount);
        return customers.size();
    }

    private long populateOrderAttributes(Model model) {
        List<Order> orders = orderService.listAll("orderTime", Sort.Direction.ASC);
        Map<OrderStatus, Long> orderStatusMap = orders.stream()
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));
        model.addAttribute("orderStatusMap", orderStatusMap);
        return orders.size();
    }

    private long populateArticleAttributes(Model model) {
        List<Article> articles = articleService.listAll("title", Sort.Direction.ASC);
        long articleMenuBoundCount = articles.stream()
                .filter(article -> article.getPublished() &&
                        (article.getArticleType() == ArticleType.NAVIGATION || article.getArticleType() == ArticleType.FOOTER))
                .count();
        Map<String, Long> articleStatusMap = articles.stream()
                .collect(Collectors.groupingBy(
                        article -> {
                            if (!article.getPublished()) {
                                return "NOT_PUBLISHED";
                            } else if (article.getArticleType() == ArticleType.FOOTER || article.getArticleType() == ArticleType.NAVIGATION) {
                                return "MENU-BOUND";
                            } else {
                                return article.getArticleType().name();
                            }
                        },
                        Collectors.counting()
                ));
        model.addAttribute("articleMenuBoundCount", articleMenuBoundCount);
        model.addAttribute("articleStatusMap", articleStatusMap);
        return articles.size();
    }

    private long populateCarouselAttributes(Model model) {
        List<Carousel> carousels = carouselService.listAll();
        Map<CarouselType, Long> carouselTypeMap = carousels.stream()
                .collect(Collectors.groupingBy(Carousel::getType, Collectors.counting()));
        model.addAttribute("carouselTypeMap", carouselTypeMap);
        return carousels.size();
    }

    private void loadSettingsToModel(List<Setting> settings, Model model) {
        settings.forEach(setting -> model.addAttribute(setting.getKey(), setting.getValue()));
    }
}
