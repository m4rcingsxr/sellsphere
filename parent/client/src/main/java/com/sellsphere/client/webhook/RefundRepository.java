package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    Optional<Refund> findByStripeId(String stripeId);

}
