package com.sellsphere.admin.review;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql", "/sql/reviews.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void givenReviewId_whenFindingById_thenReturnReview() {
        // When
        Optional<Review> reviewOptional = reviewRepository.findById(1);

        // Then
        assertTrue(reviewOptional.isPresent(), "Review with ID 1 should exist.");
        assertEquals(5, reviewOptional.get().getRate(), "Review rating should be 5.");
        assertEquals("Amazing quality!", reviewOptional.get().getHeadline(), "Review headline should match.");
    }

    @Test
    void givenApprovedReviews_whenFindingAll_thenReturnApprovedReviewsOnly() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 10, "reviewTime", Sort.Direction.DESC);

        // When
        Page<Review> result = reviewRepository.findAll("approved", pageable);

        // Then
        assertTrue(result.getContent().stream().allMatch(Review::getApproved), "All reviews should be approved.");
    }

    @Test
    void givenReview_whenSaving_thenReviewIsSaved() {
        // Given
        Review newReview = new Review();
        newReview.setRate(4);
        newReview.setHeadline("Good but pricey");
        newReview.setComment("The product is good but a bit expensive for the features offered.");
        newReview.setReviewTime(LocalDate.now());
        newReview.setApproved(true);
        newReview.setProduct(entityManager.find(Product.class, 1));
        newReview.setCustomer(entityManager.find(Customer.class, 1));

        // When
        Review savedReview = reviewRepository.save(newReview);

        // Then
        assertNotNull(savedReview.getId(), "Review ID should not be null after saving.");
        assertEquals("Good but pricey", savedReview.getHeadline(), "Review headline should match.");
    }

    @Test
    void givenReviewId_whenDeleting_thenReviewIsDeleted() {
        // When
        reviewRepository.deleteById(1);

        // Then
        Optional<Review> deletedReview = reviewRepository.findById(1);
        assertFalse(deletedReview.isPresent(), "Review with ID 1 should be deleted.");
    }
}
