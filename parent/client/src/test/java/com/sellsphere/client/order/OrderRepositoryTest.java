package com.sellsphere.client.order;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

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

    @Test
    void givenCustomerAndKeyword_whenFindAllByCustomerAndKeyword_thenReturnPagedOrders() {
        Integer customerId = 1;
        String keyword = "sampleProduct";
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Order> result = orderRepository.findAllByCustomerAndKeyword(customerId, keyword, pageRequest);

        assertNotNull(result, "The result should not be null");
        assertFalse(result.isEmpty(), "The result should contain orders");
        assertTrue(result.getContent().stream().allMatch(order ->
                                                                 order.getTransaction().getCustomer().getId().equals(
                                                                         customerId)
                                                                         || order.getOrderDetails().stream().anyMatch(
                                                                         detail ->
                                                                                 detail.getProduct().getName().toLowerCase().contains(
                                                                                         keyword.toLowerCase())
                                                                 )
        ), "Each order should match the customer ID or contain the keyword in the product name");
    }

    @Test
    void givenCustomer_whenFindAllByTransactionCustomer_thenReturnPagedOrders() {
        Customer customer = new Customer();
        customer.setId(1);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Order> result = orderRepository.findAllByTransactionCustomer(customer, pageRequest);

        assertNotNull(result, "The result should not be null");
        assertFalse(result.isEmpty(), "The result should contain orders");
        assertTrue(result.getContent().stream()
                           .allMatch(order -> order.getTransaction().getCustomer().equals(customer)), "Each order should belong to the given customer");
    }

    @Test
    void givenOrderIdAndCustomer_whenFindByIdAndTransactionCustomer_thenReturnOrder() {
        Integer orderId = 1;
        Customer customer = new Customer();
        customer.setId(1);

        Optional<Order> result = orderRepository.findByIdAndTransactionCustomer(orderId, customer);

        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(orderId, result.get().getId(), "The order ID should match the specified order ID");
        assertEquals(customer.getId(), result.get().getTransaction().getCustomer().getId(), "The customer should match the specified customer");
    }

    @Test
    void givenCustomer_whenFindAllByTransactionCustomerWithoutPagination_thenReturnListOfOrders() {
        Customer customer = new Customer();
        customer.setId(1);

        List<Order> result = orderRepository.findAllByTransactionCustomer(customer);

        assertNotNull(result, "The result should not be null");
        assertFalse(result.isEmpty(), "The result should contain orders");
        assertTrue(result.stream()
                           .allMatch(order -> order.getTransaction().getCustomer().equals(customer)), "Each order should belong to the given customer");
    }
}
