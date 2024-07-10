package com.sellsphere.client.review;

import com.sellsphere.common.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Integer> {

    Optional<ReviewVote> findByReviewIdAndCustomerId(Integer reviewId, Integer customerId);

    @Query("SELECT v FROM ReviewVote v WHERE v.review.product.id = ?1 AND v.customer.id = ?2")
    List<ReviewVote> findAllByProductAndCustomer(Integer productId, Integer customerId);
}
