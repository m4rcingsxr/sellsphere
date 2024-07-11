package com.sellsphere.admin.review;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends SearchRepository<Review, Integer> {

    @Override
    @Query("SELECT r FROM Review r WHERE LOWER(CONCAT(r.id, ' ', r.product.name, ' ', r.customer.firstName, ' ', r.customer.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> findAll(@Param("keyword") String keyword, Pageable pageable);

}
