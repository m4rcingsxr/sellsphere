package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryService;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.question.QuestionService;
import com.sellsphere.client.question.QuestionVoteService;
import com.sellsphere.client.review.ReviewService;
import com.sellsphere.client.review.ReviewVoteService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProductController {

    public static final String PRODUCTS_PATH = "product/products";

    private final CategoryService categoryService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final ReviewService reviewService;
    private final ReviewVoteService reviewVoteService;
    private final QuestionService questionService;
    private final QuestionVoteService questionVoteService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/c/{category_alias}")
    public String viewProductsByCategory(
            @PathVariable(value = "category_alias") String alias,
            Model model) throws CategoryNotFoundException {
        String pageTitle = "Products by category: ";

        Category category = categoryService.getCategoryByAlias(alias);
        List<Category> categoryParentList = categoryService.getCategoryParents(category);

        BigDecimal minPrice = productService.findMinPriceForCategory(category);
        BigDecimal maxPrice = productService.findMaxPriceForCategory(category);

        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("categoryParentList", categoryParentList);
        model.addAttribute("pageTitle", pageTitle.concat(category.getName()));
        model.addAttribute("category", category);

        return PRODUCTS_PATH;
    }

    @GetMapping("/p/search")
    public String viewProductsByKeyword(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {

        BigDecimal minPrice = productService.findMinPrice();
        BigDecimal maxPrice = productService.findMaxPrice();

        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("keyword", keyword);
        return PRODUCTS_PATH;
    }

    @GetMapping("/p/{product_alias}")
    public String viewProduct(
            @PathVariable("product_alias") String productAlias,
            Model model,
            Principal principal
    ) throws ProductNotFoundException, CustomerNotFoundException {
        Product product = productService.findByAlias(productAlias);
        Customer customer = principal != null ? getAuthenticatedCustomer(principal) : null;
        List<Category> categoryParentList = categoryService.getCategoryParents(
                product.getCategory());

        product.isOnTheWishlist(customer);

        List<Review> reviewList = reviewService.getFirst5ApprovedReviews(product);
        Map<Integer, Float> ratingPercentages = reviewService.calculateRatingPercentages(product);
        boolean customerReviewPermission = reviewService.hasCustomerReviewPermission(customer, product);
        boolean reviewPosted = reviewService.hasCustomerPostedReview(customer, product);

        List<Question> questionList = questionService.find5MostPopularQuestions(product);

        reviewVoteService.markReviewsVotedForProductByCustomer(reviewList, product, customer);
        questionVoteService.markQuestionVotedForProductByCustomer(questionList, product, customer);

        model.addAttribute("product", product);
        model.addAttribute("categoryParentList", categoryParentList);
        model.addAttribute("product", product);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("questionList", questionList);
        model.addAttribute("ratingPercentages", ratingPercentages);
        model.addAttribute("reviewPermission", customerReviewPermission);
        model.addAttribute("reviewPosted", reviewPosted);
        model.addAttribute("customer", customer);
        model.addAttribute("review", new Review());
        model.addAttribute("question", new Question());

        Category categoryProduct = new Category();
        categoryProduct.setName(product.getName());

        categoryParentList.add(categoryProduct);

        return "product/product_detail";
    }

    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }
}
