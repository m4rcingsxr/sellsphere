package com.sellsphere.client.review;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findAllByCustomerAndProduct(Customer customer, Product product);

    Page<Review> findAllByProductAndApprovedIsTrue(Product product, Pageable pageable);

    List<Review> findAllByCustomer(Customer customer);

    List<Review> findAllByProductAndApprovedIsTrue(Product product);

    @Query("UPDATE Review r SET r.votes = (SELECT SUM(v.votes) FROM ReviewVote v WHERE v.review.id = ?1) WHERE r.id = ?1")
    @Modifying
    void updateVoteCount(Integer reviewId);

    @Query("SELECT r.votes FROM Review r WHERE r.id = :reviewId")
    Integer findVotesByReviewId(@Param("reviewId") Integer reviewId);
}
