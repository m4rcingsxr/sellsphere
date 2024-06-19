package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends SearchRepository<PaymentIntent, Integer> {

    @Override
    @Query("SELECT p FROM PaymentIntent p WHERE LOWER(CONCAT(p.id, ' ', p.currency.code, ' ', p" +
            ".customer.email, ' ', p.status)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PaymentIntent> findAll(@Param("keyword") String keyword, Pageable pageRequest);

    Optional<PaymentIntent> findByStripeId(String stripeId);

}
