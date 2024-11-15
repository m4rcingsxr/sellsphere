package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.PaymentIntent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentIntentRepository extends SearchRepository<PaymentIntent, Integer> {

    @Override
    @Query("SELECT p FROM PaymentIntent p WHERE LOWER(CONCAT(p.id, ' ', p" +
            ".customer.email, ' ', p.status, ' ', p.stripeId)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PaymentIntent> findAll(@Param("keyword") String keyword, Pageable pageRequest);

}
