package com.sellsphere.client.order;

import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private static final int ORDER_PER_PAGE = 10;

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the order ID
     * @return the order
     * @throws OrderNotFoundException if the order is not found
     */
    public Order getById(Integer orderId) throws OrderNotFoundException {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public Order createOrder(PaymentIntent transaction) {

        Order order = Order.builder()
                .transaction(transaction)
                .orderTime(LocalDateTime.now())
                .build();

        List<CartItem> cart = cartItemRepository.findByCustomer(transaction.getCustomer());
        cart.forEach(order::addOrderDetail);

        order.addOrderTrack(
                OrderTrack.builder()
                        .updatedTime(LocalDate.now())
                        .status(OrderStatus.NEW)
                        .notes(OrderStatus.NEW.getNote())
                        .build()
        );

        transaction.setOrder(order);

        return orderRepository.save(order);
    }

    /**
     * Lists orders by page and optionally filters by keyword.
     *
     * @param customer   the customer whose orders to list
     * @param pageNumber the page number
     * @param sortField  the field to sort by
     * @param sortDir    the sort direction ('asc' or 'desc')
     * @param keyword    the keyword to filter by
     * @return a page of orders
     */
    public Page<Order> listByPage(Customer customer, Integer pageNumber, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("desc") ? sort.descending() : sort.ascending();

        PageRequest pageRequest = PageRequest.of(pageNumber, ORDER_PER_PAGE,
                                                 sort
        );

        if (keyword != null) {
            return orderRepository.findAllByCustomerAndKeyword(customer.getId(),
                                                               keyword,
                                                               pageRequest
            );
        } else {
            return orderRepository.findAllByTransactionCustomer(customer, pageRequest);
        }
    }

    /**
     * Sets an order as return requested.
     *
     * @param request  the return request
     * @param customer the customer requesting the return
     * @throws OrderNotFoundException          if the order is not found
     * @throws ReturnRequestAlreadyPlacedException if a return request has already been placed for the order
     */
    public void setOrderReturnRequested(OrderReturnRequest request,
                                        Customer customer)
            throws OrderNotFoundException, ReturnRequestAlreadyPlacedException {
        Order order = orderRepository
                .findByIdAndTransactionCustomer(request.getOrderId(), customer)
                .orElseThrow(OrderNotFoundException::new);

        validateReturnRequest(order);
        addReturnRequestOrderTrack(request, order);
        orderRepository.save(order);
    }

    private void validateReturnRequest(Order order) throws ReturnRequestAlreadyPlacedException {
        validateOrderDelivered(order);
        validateReturnRequestNotPlaced(order);
    }

    private void validateOrderDelivered(Order order) {
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order is not delivered");
        }
    }

    // Validates that a return request has not already been placed for the order.
    private void validateReturnRequestNotPlaced(Order order)
            throws ReturnRequestAlreadyPlacedException {
        if (order.isReturnRequested()) {
            throw new ReturnRequestAlreadyPlacedException(
                    "Return request already placed.");
        }
    }

    private void addReturnRequestOrderTrack(OrderReturnRequest request, Order order) {

        OrderTrack track = createOrderTrackForReturnRequest(request, order);
        order.getOrderTracks().add(track);
    }

    private OrderTrack createOrderTrackForReturnRequest(
            OrderReturnRequest request, Order order) {
        OrderTrack track = new OrderTrack();
        track.setOrder(order);
        track.setUpdatedTime(LocalDate.now());
        track.setStatus(OrderStatus.RETURN_REQUESTED);

        String notes = "Reason: " + request.getReason();
        if (request.getNote() != null && !request.getNote().isBlank()) {
            notes += ". Customer note: " + request.getNote();
        }

        track.setNotes(notes);

        return track;
    }

    public List<Product> findBoughtProducts(Customer customer) {
        List<Order> allCustomerOrders = orderRepository.findAllByTransactionCustomer(customer);
        return allCustomerOrders.stream()
                .map(Order::getOrderDetails)
                .flatMap(List::stream)
                .map(OrderDetail::getProduct)
                .toList();
    }

}
