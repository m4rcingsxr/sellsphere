package com.sellsphere.client.order;

import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1);
    }

    @Test
    void givenValidOrderId_whenGetById_thenReturnOrder() throws OrderNotFoundException {
        // Given
        Order order = new Order();
        order.setId(1);
        given(orderRepository.findById(1)).willReturn(Optional.of(order));

        // When
        Order foundOrder = orderService.getById(1);

        // Then
        assertEquals(order, foundOrder, "Returned order should match the given order ID");
        verify(orderRepository).findById(1);
    }

    @Test
    void givenInvalidOrderId_whenGetById_thenThrowOrderNotFoundException() {
        // Given
        given(orderRepository.findById(1)).willReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> orderService.getById(1),
                     "Should throw OrderNotFoundException if order ID is invalid");
    }

    @Test
    void givenCustomerAndPagination_whenListByPage_thenReturnPaginatedOrders() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("orderTime").ascending());
        Page<Order> paginatedOrders = new PageImpl<>(List.of(new Order()));
        given(orderRepository.findAllByTransactionCustomer(customer, pageRequest)).willReturn(paginatedOrders);

        // When
        Page<Order> result = orderService.listByPage(customer, 0, "orderTime", "asc", null);

        // Then
        assertEquals(paginatedOrders, result, "The paginated orders should be returned");
        verify(orderRepository).findAllByTransactionCustomer(customer, pageRequest);
    }

    @Test
    void givenKeywordForSearch_whenListByPage_thenReturnFilteredPaginatedOrders() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("orderTime").ascending());
        String keyword = "camera";
        Page<Order> filteredOrders = new PageImpl<>(List.of(new Order()));
        given(orderRepository.findAllByCustomerAndKeyword(customer.getId(), keyword, pageRequest)).willReturn(filteredOrders);

        // When
        Page<Order> result = orderService.listByPage(customer, 0, "orderTime", "asc", keyword);

        // Then
        assertEquals(filteredOrders, result, "The filtered paginated orders should be returned");
        verify(orderRepository).findAllByCustomerAndKeyword(customer.getId(), keyword, pageRequest);
    }

    @Test
    void givenDeliveredOrder_whenSetOrderReturnRequested_thenOrderStatusUpdatedToReturnRequested() throws OrderNotFoundException, ReturnRequestAlreadyPlacedException {
        // Given
        Order order = new Order();
        order.addOrderTrack(OrderTrack.builder().status(OrderStatus.DELIVERED).build());
        order.setOrderTime(LocalDateTime.now());

        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderId(1);
        request.setReason("Defective item");

        given(orderRepository.findByIdAndTransactionCustomer(1, customer)).willReturn(Optional.of(order));

        // When
        orderService.setOrderReturnRequested(request, customer);

        // Then
        assertTrue(order.isReturnRequested(), "Order should be marked as return requested");
        assertEquals(OrderStatus.RETURN_REQUESTED, order.getOrderTracks().get(order.getOrderTracks().size() - 1).getStatus(),
                     "Latest order track should be RETURN_REQUESTED");

        verify(orderRepository).findByIdAndTransactionCustomer(1, customer);
        verify(orderRepository).save(order);
    }

    @Test
    void givenReturnRequestedOrder_whenSetOrderReturnRequested_thenThrowIllegalStateException() throws OrderNotFoundException {
        // Given
        Order order = new Order();
        order.setOrderTracks(List.of(
                OrderTrack.builder().updatedTime(LocalDate.now().minusDays(1)).status(OrderStatus.DELIVERED).build(),
                OrderTrack.builder().updatedTime(LocalDate.now()).status(OrderStatus.RETURN_REQUESTED).build()
        ));

        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderId(1);

        given(orderRepository.findByIdAndTransactionCustomer(1, customer)).willReturn(Optional.of(order));

        // When / Then
        assertThrows(IllegalStateException.class, () -> orderService.setOrderReturnRequested(request, customer),
                     "Should throw IllegalStateException if the order is not in a deliverable state for a return request");
    }

    @Test
    void givenCustomer_whenFindBoughtProducts_thenReturnListOfProducts() {
        // Given
        Order order1 = new Order();
        OrderDetail detail1 = new OrderDetail();
        Product product1 = new Product();
        detail1.setProduct(product1);
        order1.setOrderDetails(List.of(detail1));

        Order order2 = new Order();
        OrderDetail detail2 = new OrderDetail();
        Product product2 = new Product();
        detail2.setProduct(product2);
        order2.setOrderDetails(List.of(detail2));

        given(orderRepository.findAllByTransactionCustomer(customer)).willReturn(List.of(order1, order2));

        // When
        List<Product> boughtProducts = orderService.findBoughtProducts(customer);

        // Then
        assertEquals(List.of(product1, product2), boughtProducts, "Bought products should be returned");

        verify(orderRepository).findAllByTransactionCustomer(customer);
    }
}
