package com.sellsphere.admin.review;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Review;
import com.sellsphere.common.entity.ReviewNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(scripts = {
        "/sql/categories.sql",
        "/sql/brands.sql",
        "/sql/brands_categories.sql",
        "/sql/customers.sql",
        "/sql/products.sql",
        "/sql/reviews.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    void givenExistingReviewId_whenGet_thenReturnReview() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;

        // When
        Review review = reviewService.get(reviewId);

        // Then
        assertNotNull(review, "Review should be found.");
        assertEquals("Amazing quality!", review.getHeadline(), "Headline should match.");
    }

    @Test
    void givenNonExistingReviewId_whenGet_thenThrowReviewNotFoundException() {
        // Given
        Integer reviewId = 999;

        // When/Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.get(reviewId));
    }

    @Test
    void givenPageNumber_whenListPage_thenReturnPagedReviews() {
        // Given
        int pageNum = 0;
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(), "reviewList", "reviewTime", Sort.Direction.ASC, null);
        helper.setKeyword("Canon");

        // When
        reviewService.listPage(pageNum, helper);

        // Then
        assertFalse(helper.getContent().isEmpty(), "There should be reviews returned for the keyword.");
    }

    @Test
    void givenReview_whenSave_thenReviewIsSaved() {
        // Given
        Review review = new Review();
        review.setRate(4);
        review.setHeadline("Test Review");
        review.setComment("Test comment");
        review.setApproved(true);
        review.setReviewTime(LocalDate.now());
        review.setProduct(productRepository.findById(1).orElseThrow());

        // When
        Review savedReview = reviewService.save(review);

        // Then
        assertNotNull(savedReview.getId(), "Saved review ID should not be null.");
        assertEquals("Test Review", savedReview.getHeadline(), "Review headline should match.");
    }

    @Test
    void givenExistingReviewId_whenDelete_thenReviewIsDeleted() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;

        // When
        reviewService.delete(reviewId);

        // Then
        Optional<Review> deletedReview = reviewRepository.findById(reviewId);
        assertFalse(deletedReview.isPresent(), "Review should be deleted.");
    }

    @Test
    void givenReviewIdAndStatus_whenUpdateReviewApproveStatus_thenStatusIsUpdated() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;
        Boolean newStatus = true;

        // When
        reviewService.updateReviewApproveStatus(reviewId, newStatus);

        // Then
        Review updatedReview = reviewService.get(reviewId);
        assertTrue(updatedReview.getApproved(), "Review approval status should be updated to true.");
    }

    @Test
    void givenReviewWithProduct_whenUpdatingApproval_thenProductSummaryIsUpdated() throws ReviewNotFoundException {
        // Given
        Integer reviewId = 1;
        Review review = reviewService.get(reviewId);
        Product product = review.getProduct();
        int oldReviewCount = product.getReviewCount();
        float oldAverageRating = product.getAverageRating();

        // When
        reviewService.updateReviewApproveStatus(reviewId, true);

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(oldReviewCount + 1, updatedProduct.getReviewCount(), "Product review count should be incremented.");
        assertNotEquals(oldAverageRating, updatedProduct.getAverageRating(), "Product average rating should be updated.");
    }
}
