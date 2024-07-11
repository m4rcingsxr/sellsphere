package com.sellsphere.admin.review;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

}
