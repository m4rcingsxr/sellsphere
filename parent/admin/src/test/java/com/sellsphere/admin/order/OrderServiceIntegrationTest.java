package com.sellsphere.admin.order;

import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.OrderNotFoundException;
import com.sellsphere.common.entity.OrderStatus;
import com.sellsphere.common.entity.OrderTrackNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql", "classpath:sql/payment_intents.sql", "classpath:sql/orders.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    void givenOrderId_whenGetById_thenReturnOrder() throws Exception {
        int orderId = 1;

        // Given
        Order order = orderService.getById(orderId);

        // Then
        assertNotNull(order, "Order should be found");
        assertEquals(orderId, order.getId(), "Order ID should match the requested ID");
    }

    @Test
    void givenInvalidOrderId_whenGetById_thenThrowOrderNotFoundException() {
        int invalidOrderId = 999;

        assertThrows(OrderNotFoundException.class, () -> orderService.getById(invalidOrderId),
                     "Should throw OrderNotFoundException for invalid ID");
    }

    @Test
    void givenDuplicateOrderTrack_whenAddOrderTrack_thenThrowException() {
        int orderId = 1;
        OrderStatus existingStatus = OrderStatus.NEW;

        assertThrows(IllegalArgumentException.class, () -> orderService.addOrderTrack(orderId, existingStatus, "Duplicate track"),
                     "Adding a duplicate track should throw IllegalArgumentException");
    }

    @Test
    void givenOutOfOrderTrack_whenAddOrderTrack_thenThrowException() {
        int orderId = 1;
        OrderStatus incorrectStatus = OrderStatus.SHIPPING;

        assertThrows(IllegalArgumentException.class, () -> orderService.addOrderTrack(orderId, incorrectStatus, "Out of sequence"),
                     "Adding an out-of-sequence track should throw IllegalArgumentException");
    }

    @Test
    void givenOrderId_whenDeleteTrack_thenTrackIsDeleted() throws Exception {
        int orderId = 1;
        int trackId = orderService.listOrderTracks(orderId).get(0).getId(); // Get an existing track ID

        // Given
        orderService.deleteTrack(trackId);

        // Then
        assertThrows(OrderTrackNotFoundException.class, () -> orderService.deleteTrack(trackId),
                     "Deleting the same track again should throw OrderTrackNotFoundException");
    }
}
