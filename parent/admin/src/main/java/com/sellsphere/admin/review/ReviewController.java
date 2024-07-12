package com.sellsphere.admin.review;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Review;
import com.sellsphere.common.entity.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/reviews/page/0?sortField=reviewTime&sortDir=desc";

    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/reviews/page/{pageNum}")
    public String listPage(@PagingAndSortingParam(listName = "reviewList", moduleURL = "/reviews") PagingAndSortingHelper helper,
                           @PathVariable int pageNum) {
        reviewService.listPage(pageNum, helper);

        return "review/reviews";
    }

    @GetMapping("/reviews/edit/{id}")
    public String showReviewForm(@PathVariable("id") Integer id,
                                 Model model) throws ReviewNotFoundException {
        Review review = reviewService.get(id);
        model.addAttribute("review", review);

        return "review/review_form";
    }

    @PostMapping("/reviews/save")
    public String updateReview(@ModelAttribute Review review) {
        Review savedReview = reviewService.save(review);

        return DEFAULT_REDIRECT_URL + savedReview.getId();
    }


}
