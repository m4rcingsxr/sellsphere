package com.sellsphere.admin.review;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Review;
import com.sellsphere.common.entity.ReviewNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String updateReview(@ModelAttribute @Valid Review review, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("review", review);
            return "review/review_form";
        }

        Review savedReview = reviewService.save(review);

        return DEFAULT_REDIRECT_URL + savedReview.getId();
    }

    @GetMapping("/reviews/details/{id}")
    public String viewReviewDetails(@PathVariable Integer id, Model model) throws ReviewNotFoundException {
        Review review = reviewService.get(id);
        model.addAttribute("review", review);

        return "review/review_detail_modal";
    }

    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Integer id, RedirectAttributes ra) throws ReviewNotFoundException {
        reviewService.delete(id);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Successfully removed review with ID [" + id + "]");

        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/reviews/{id}/approved/{status}")
    public String updateReviewApproveStatus(@PathVariable Integer id, @PathVariable Boolean status, RedirectAttributes ra) throws ReviewNotFoundException {
        reviewService.updateReviewApproveStatus(id, status);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                             "The review ID " + id + " has been "
                                     + (status ? "approved" : "rejected"));

        return DEFAULT_REDIRECT_URL;
    }

}
