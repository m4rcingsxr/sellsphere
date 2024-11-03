package com.sellsphere.admin.order;

import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.OrderDetail;
import com.sellsphere.common.entity.OrderTrack;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql", "classpath:sql/payment_intents.sql",
                "classpath:sql/orders.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void givenKeyword_whenSearchingOrders_thenReturnMatchingOrders() {
        // Given
        String keyword = "1"; // Assuming order or transaction ID contains "1"
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findAll(keyword, pageable);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Search results should not be empty for keyword '1'.");
        result.forEach(order -> assertTrue(
                order.getId().toString().contains(keyword) || order.getTransaction().getId().toString().contains(
                        keyword),
                "Order ID or Transaction ID should match the keyword."
        ));
    }

    @Test
    void givenDateRange_whenFindingOrders_thenReturnOrdersWithinRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 31, 23, 59);

        // When
        List<Order> result = orderRepository.findByOrderTimeBetween(startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Orders within the specified date range should be returned.");
        result.forEach(order -> assertTrue(
                order.getOrderTime().isAfter(startTime) && order.getOrderTime().isBefore(endTime),
                "Order time should be within the specified range."
        ));
    }

    @Test
    void givenPagingParameters_whenFindingAllOrders_thenReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When
        Page<Order> result = orderRepository.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size(), "Page size should match the specified limit.");
    }

    @Test
    void givenOrder_whenDeleting_thenDeleteOrderAndAssociatedDetails() {
        // Given
        Integer orderId = 1; // assuming an order with this ID exists in orders.sql

        Optional<Order> orderOptional = orderRepository.findById(orderId);
        assertTrue(orderOptional.isPresent(), "Order with ID " + orderId + " should exist.");

        Order order = orderOptional.get();
        List<OrderDetail> details = order.getOrderDetails();
        List<OrderTrack> tracks = order.getOrderTracks();

        // When
        orderRepository.delete(order);

        // Then
        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        assertFalse(deletedOrder.isPresent(), "Order should be deleted.");

        details.forEach(detail ->
                                assertNull(entityManager.find(OrderDetail.class, detail.getId()),
                                           "Order detail should be deleted."
                                )
        );

        tracks.forEach(track ->
                               assertNull(entityManager.find(OrderTrack.class, track.getId()),
                                          "Order track should be deleted."
                               )
        );
    }

}