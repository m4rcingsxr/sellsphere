package com.sellsphere.admin.order;

import com.sellsphere.common.entity.OrderStatus;
import com.sellsphere.common.entity.OrderTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderTrackRepository extends JpaRepository<OrderTrack, Integer> {

    Optional<OrderTrack> findByOrderIdAndStatus(Integer orderId, OrderStatus status);

    List<OrderTrack> findByOrderId(Integer orderId);
}
