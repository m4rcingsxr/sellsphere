package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Card;
import com.sellsphere.common.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Integer> {

    List<Card> findAllByCustomer(Customer customer);

    Optional<Card> findByStripeId(String paymentMethodId);

    void deleteByStripeId(String stripeId);

}