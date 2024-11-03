package com.sellsphere.admin.review;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Review;
import com.sellsphere.common.entity.ReviewNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewVoteRepository reviewVoteRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void givenValidReviewId_whenGetReview_thenReturnReview() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;
        Review review = new Review();
        review.setId(reviewId);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // When
        Review foundReview = reviewService.get(reviewId);

        // Then
        assertEquals(reviewId, foundReview.getId(), "Review ID should match the requested ID.");
        then(reviewRepository).should().findById(reviewId);
    }

    @Test
    void givenNonExistingReviewId_whenGetReview_thenThrowReviewNotFoundException() {
        // Given
        Integer reviewId = 999;
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.get(reviewId));
        then(reviewRepository).should().findById(reviewId);
    }

    @Test
    void givenReview_whenSaving_thenReturnSavedReview() {
        // Given
        Review review = new Review();
        review.setHeadline("Test Review");
        given(reviewRepository.save(review)).willReturn(review);

        // When
        Review savedReview = reviewService.save(review);

        // Then
        assertEquals("Test Review", savedReview.getHeadline(), "Saved review headline should match.");
        then(reviewRepository).should().save(review);
    }

    @Test
    void givenValidReviewId_whenDeleting_thenReviewIsDeleted() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;
        Review review = new Review();
        review.setId(reviewId);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // When
        reviewService.delete(reviewId);

        // Then
        then(reviewVoteRepository).should().deleteAllByReview(review);
        then(reviewRepository).should().delete(review);
    }

    @Test
    void givenNonExistingReviewId_whenDeleting_thenThrowReviewNotFoundException() {
        // Given
        Integer reviewId = 999;
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.delete(reviewId));
        then(reviewRepository).should().findById(reviewId);
    }

    @Test
    void givenReviewIdAndStatus_whenUpdatingApproval_thenStatusIsUpdatedAndProductSummaryUpdated() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;
        Review review = new Review();
        review.setId(reviewId);
        review.setRate(4);
        Product product = new Product();
        product.setAverageRating(4.0f);
        product.setReviewCount(2);
        review.setProduct(product);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // When
        reviewService.updateReviewApproveStatus(reviewId, true);

        // Then
        assertTrue(review.getApproved(), "Review approval status should be true.");
        assertEquals(3, product.getReviewCount(), "Product review count should be updated.");
        then(reviewRepository).should().save(review);
    }

    @Test
    void givenNonExistingReviewId_whenUpdatingApproval_thenThrowReviewNotFoundException() {
        // Given
        Integer reviewId = 999;
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.updateReviewApproveStatus(reviewId, true));
        then(reviewRepository).should().findById(reviewId);
    }
}
