package com.sellsphere.client.review;

// todo: for question and review write for customer table that will list their questions and reviews

import com.sellsphere.client.category.CategoryService;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.order.OrderService;
import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class ReviewController {

    private final CustomerService customerService;
    private final ReviewService reviewService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewVoteService reviewVoteService;
    private final CategoryService categoryService;

    @GetMapping("/reviews")
    public String listFirstPageProductsWithoutReview(Principal principal, Model model) throws CustomerNotFoundException {
        Customer authenticatedCustomer = getAuthenticatedCustomer(principal);

        List<Product> productList = orderService.findBoughtProducts(authenticatedCustomer);
        Map<Product, Review> productMap = reviewService.mapProductsByReviewStatus(authenticatedCustomer, productList);

        Review review = new Review();

        model.addAttribute("customer", authenticatedCustomer);
        model.addAttribute("productMap", productMap);
        model.addAttribute("review", review);

        return "review/reviews";
    }

    @PostMapping("/reviews/create")
    public String createReview(@ModelAttribute("review") Review review,
                               Principal principal,
                               RedirectAttributes ra) throws ProductNotFoundException, CustomerNotFoundException {
        Product product = productService.findById(review.getProduct().getId());
        Customer customer = getAuthenticatedCustomer(principal);

        boolean reviewPermission = reviewService.hasCustomerReviewPermission(customer, product);

        if(reviewPermission) {
            reviewService.save(review);
            ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Successfully posted review.");
        } else {
            ra.addFlashAttribute(Constants.ERROR_MESSAGE, "Customer must buy the product to post the review.");
        }

        return "redirect:/reviews";
    }

    @GetMapping("/reviews/detail/{id}")
    public String detailReview(@PathVariable Integer id,  Model model)
            throws ReviewNotFoundException {
        Review review = reviewService.getReview(id);
        model.addAttribute("review", review);

        return "review/review_detail_sidebar";
    }

    @GetMapping("/reviews/p/{product_alias}/page/{pageNum}")
    public String pageReviews(@PathVariable("product_alias") String productAlias,
                              @PathVariable Integer pageNum,
                              @RequestParam(value = "sortField", required = false) String sortField,
                              Principal principal,
                              Model model) throws ProductNotFoundException, CustomerNotFoundException {
        Product product = productService.findByAlias(productAlias);
        Customer customer = getAuthenticatedCustomer(principal);

        Page<Review> reviewPage = reviewService.pageReviews(product, pageNum, sortField);
        Map<Integer, Float> ratingPercentages = reviewService.calculateRatingPercentages(product);
        List<Category> listCategoryParents = categoryService.getCategoryParents(product.getCategory());

        boolean reviewPermission = reviewService.hasCustomerReviewPermission(customer, product);
        boolean reviewPosted = reviewService.hasCustomerPostedReview(customer, product);

        reviewVoteService.markReviewsVotedForProductByCustomer(reviewPage.getContent(), product, customer);

        model.addAttribute("product", product);
        model.addAttribute("reviewList", reviewPage.getContent());
        model.addAttribute("ratingPercentages", ratingPercentages);
        model.addAttribute("listCategoryParents", listCategoryParents);
        model.addAttribute("reviewPermission", reviewPermission);
        model.addAttribute("reviewPosted", reviewPosted);
        model.addAttribute("customer", customer);
        model.addAttribute("review", new Review());

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("sortField", sortField);

        return "review/product_reviews";
    }

    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        if(principal != null) {
            return customerService.getByEmail(principal.getName());
        }

        return null;
    }

}

