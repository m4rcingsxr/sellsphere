package com.sellsphere.client.order;

import com.sellsphere.client.address.AddressRepository;
import com.sellsphere.client.checkout.CourierRepository;
import com.sellsphere.client.checkout.PaymentIntentRepository;
import com.sellsphere.client.customer.CustomerRepository;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.common.entity.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/brands_categories.sql",
                "classpath:sql/products.sql", "classpath:sql/payment_intents.sql",
                "classpath:sql/orders.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private CourierRepository courierRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentIntentRepository paymentIntentRepository;

    @Test
    void givenNewPaymentIntent_whenCreateOrder_thenOrderIsCreatedWithDetails() {
        // Given
        Customer customer = customerRepository.findById(1).orElseThrow();
        Address address = addressRepository.findById(1).orElseThrow();
        Currency currency = currencyRepository.findById(1).orElseThrow();
        Courier courier = courierRepository.findById(1).orElseThrow();

        PaymentIntent paymentIntent = PaymentIntent.builder()
                .stripeId("pi_12345")
                .amount(5000L)
                .shippingAmount(500L)
                .shippingTax(100L)
                .taxAmount(400L)
                .shippingAddress(address)
                .targetCurrency(currency)
                .customer(customer)
                .status("succeeded")
                .created(System.currentTimeMillis())
                .courier(courier)
                .build();


        // When
        Order createdOrder = orderService.createOrder(paymentIntentRepository.save(paymentIntent));

        // Then
        assertNotNull(createdOrder.getId(), "Created order should have an ID");
        assertEquals(paymentIntent, createdOrder.getTransaction(), "Order transaction should match given payment intent");
        assertNull(createdOrder.getOrderDetails(), "Order should have order details");
        assertTrue(createdOrder.getOrderTracks().stream()
                           .anyMatch(track -> track.getStatus() == OrderStatus.NEW),
                   "Order should have a NEW status track");
    }

    @Test
    void givenExistingOrderId_whenGetById_thenOrderIsReturned() throws OrderNotFoundException {
        // Given
        Integer orderId = 1;

        // When
        Order order = orderService.getById(orderId);

        // Then
        assertNotNull(order, "Order should not be null");
        assertEquals(orderId, order.getId(), "Order ID should match the given ID");
    }

    @Test
    void givenCustomerAndPaginationParams_whenListByPage_thenOrdersArePaginated() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        int pageNumber = 0;
        String sortField = "orderTime";

        // When
        Page<Order> ordersPage = orderService.listByPage(customer, pageNumber, sortField, Sort.Direction.ASC, null);

        // Then
        assertNotNull(ordersPage, "Orders page should not be null");
        assertTrue(ordersPage.getTotalElements() > 0, "Orders page should contain orders");
    }

    @Test
    void givenDeliveredOrder_whenSetOrderReturnRequested_thenStatusIsUpdatedToReturnRequested() throws OrderNotFoundException, ReturnRequestAlreadyPlacedException {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        Integer orderId = 1;
        Order order = orderService.getById(orderId);
        OrderReturnRequest returnRequest = new OrderReturnRequest();
        returnRequest.setOrderId(orderId);
        returnRequest.setReason("Product is defective");

        // Simulate the delivered status in order tracks
        order.getOrderTracks().add(OrderTrack.builder()
                                           .status(OrderStatus.DELIVERED)
                                           .order(order)
                                           .updatedTime(LocalDate.now())
                                           .build());
        orderRepository.save(order);

        // When
        orderService.setOrderReturnRequested(returnRequest, customer);

        // Then
        assertTrue(order.isReturnRequested(), "Order should be marked as return requested");
        assertEquals(OrderStatus.RETURN_REQUESTED, order.getOrderTracks()
                .get(order.getOrderTracks().size() - 1)
                .getStatus(), "Latest order track should be RETURN_REQUESTED");
    }

    @Test
    void givenCustomerWithOrders_whenFindBoughtProducts_thenListOfProductsIsReturned() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);

        // When
        List<Product> boughtProducts = orderService.findBoughtProducts(customer);

        // Then
        assertNotNull(boughtProducts, "Bought products list should not be null");
        assertFalse(boughtProducts.isEmpty(), "Bought products list should not be empty");
    }
}
