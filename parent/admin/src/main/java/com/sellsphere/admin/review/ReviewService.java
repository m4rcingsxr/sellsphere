package com.sellsphere.admin.review;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Review;
import com.sellsphere.common.entity.ReviewNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int REVIEWS_PER_PAGE = 10;

    private final ReviewRepository repository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ProductRepository productRepository;

    public Review get(Integer id) throws ReviewNotFoundException {
        return repository.findById(id).orElseThrow(ReviewNotFoundException::new);
    }

    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        String sortField = helper.getSortField();
        if(sortField.equals("customer")) {
            Pageable customerPageRequest = createPageRequestSortedByCustomer(helper, pageNum);

            String keyword = helper.getKeyword();
            Page<Review> page;
            if(keyword != null) {
                page = repository.findAll(keyword, customerPageRequest);
            } else {
                page = repository.findAll(customerPageRequest);
            }

            helper.updateModelAttributes(pageNum, page);
        } else {
            helper.listEntities(pageNum, REVIEWS_PER_PAGE, repository);
        }
    }

    private Pageable createPageRequestSortedByCustomer(PagingAndSortingHelper helper, int pageNum) {
        Sort sort = Sort.by("customer.firstName", "customer.lastName");
        sort = helper.getSortDir().equals(Constants.SORT_ASCENDING) ? sort.ascending() : sort.descending();
        return PageRequest.of(pageNum, REVIEWS_PER_PAGE, sort);
    }

    public Review save(Review review) {
        return repository.save(review);
    }

    @Transactional
    public void delete(Integer id) throws ReviewNotFoundException {
        Review review = repository.findById(id).orElseThrow(ReviewNotFoundException::new);
        reviewVoteRepository.deleteAllByReview(review);

        repository.delete(review);
    }

    public void updateReviewApproveStatus(Integer id, Boolean status) throws ReviewNotFoundException {
        Review review = repository.findById(id).orElseThrow(ReviewNotFoundException::new);
        review.setApproved(status);
        updateProductReviewSummary(review);

        repository.save(review);
    }

    private void updateProductReviewSummary(Review review) {
        Product product = review.getProduct();
        Integer newRating = review.getRate();
        Float oldAverage = product.getAverageRating();
        Integer oldCount = product.getReviewCount();

        Float newAverage = ((oldAverage * oldCount) + newRating) / (oldCount + 1);
        product.setAverageRating(newAverage);
        product.setReviewCount(++oldCount);

        productRepository.save(product);
    }
}
