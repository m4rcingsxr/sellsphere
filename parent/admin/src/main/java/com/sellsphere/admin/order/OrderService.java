package com.sellsphere.admin.order;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.OrderTrackDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int ORDERS_PER_PAGE = 10;

    private final OrderRepository repository;
    private final OrderTrackRepository orderTrackRepository;
    private final OrderRepository orderRepository;

    /**
     * Lists orders by page, with optional keyword filtering, sorting, and pagination.
     *
     * @param page   the page number to retrieve.
     * @param helper a helper object containing pagination and sorting parameters.
     */
    public void listPage(int page, PagingAndSortingHelper helper) {
        helper.listEntities(page, ORDERS_PER_PAGE, repository);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the ID of the order to retrieve.
     * @return the found order.
     * @throws OrderNotFoundException if no order is found with the given ID.
     */
    public Order getById(int orderId) throws OrderNotFoundException {
        return repository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    /**
     * Adds a new track for an order based on its ID, status, and additional note.
     *
     * @param orderId     the ID of the order to update.
     * @param orderStatus the status of the new order track.
     * @param note        additional notes for the order track.
     * @return a DTO representing the newly added order track.
     * @throws OrderNotFoundException    if the order is not found.
     * @throws IllegalArgumentException  if the track already exists or is in the wrong sequence.
     */
    public OrderTrackDTO addOrderTrack(int orderId, OrderStatus orderStatus, String note) throws OrderNotFoundException {
        validateUniqueAndOrderedTrack(orderId, orderStatus);

        Order order = getById(orderId);
        OrderTrack orderTrack = createOrderTrack(order, orderStatus, note);
        orderTrackRepository.save(orderTrack);

        return buildOrderTrackDTO(orderTrack);
    }

    /**
     * Validates that the new track for an order does not already exist and follows the correct sequence.
     */
    private void validateUniqueAndOrderedTrack(int orderId, OrderStatus orderStatus) throws OrderNotFoundException {
        if (!isOrderTrackUnique(orderId, orderStatus)) {
            throw new IllegalArgumentException("Order track already exists in tracking");
        }

        Order order = getById(orderId);
        if (!isOrderTrackInCorrectOrder(order.getOrderTracks(), orderStatus)) {
            OrderStatus lastStatus = order.getOrderTracks().get(order.getOrderTracks().size() - 1).getStatus();
            throw new IllegalArgumentException("Order track is not in correct order. " + orderStatus + " cannot appear after " + lastStatus);
        }
    }

    /**
     * Checks if an order track with a specific status already exists for an order.
     *
     * @param orderId     the ID of the order.
     * @param orderStatus the status to check for uniqueness.
     * @return true if unique, false otherwise.
     */
    public boolean isOrderTrackUnique(int orderId, OrderStatus orderStatus) {
        return orderTrackRepository.findByOrderIdAndStatus(orderId, orderStatus).isEmpty();
    }

    /**
     * Verifies if the new order track status follows the correct sequence order.
     *
     * @param orderTracks existing list of order tracks for an order.
     * @param status      the new status to verify.
     * @return true if the new status follows the correct order, false otherwise.
     */
    public boolean isOrderTrackInCorrectOrder(List<OrderTrack> orderTracks, OrderStatus status) {
        return orderTracks.stream()
                .max(Comparator.comparingInt(track -> track.getStatus().ordinal()))
                .map(track -> track.getStatus().ordinal() <= status.ordinal())
                .orElse(true);
    }

    /**
     * Lists all tracks associated with an order in the correct sequence.
     *
     * @param orderId the ID of the order.
     * @return a list of DTOs representing the order tracks.
     */
    public List<OrderTrackDTO> listOrderTracks(int orderId) {
        return orderTrackRepository.findByOrderId(orderId).stream()
                .sorted(Comparator.comparingInt(track -> track.getStatus().ordinal()))
                .map(this::buildOrderTrackDTO)
                .toList();
    }

    /**
     * Deletes an order track by ID and returns the deleted track information.
     *
     * @param id the ID of the order track to delete.
     * @return a DTO representing the deleted order track.
     * @throws OrderTrackNotFoundException if no order track is found with the given ID.
     */
    public OrderTrackDTO deleteTrack(int id) throws OrderTrackNotFoundException {
        OrderTrack orderTrack = orderTrackRepository.findById(id)
                .orElseThrow(OrderTrackNotFoundException::new);
        orderTrackRepository.delete(orderTrack);

        return buildOrderTrackDTO(orderTrack);
    }

    /**
     * Sets the status for an order, adding all intermediate statuses if required.
     *
     * @param orderId   the ID of the order to update.
     * @param newStatus the final status to be set for the order.
     * @throws OrderNotFoundException   if the order is not found.
     * @throws IllegalArgumentException if the status already exists.
     */
    public void setOrderStatus(int orderId, OrderStatus newStatus) throws OrderNotFoundException {
        Order order = getById(orderId);
        List<OrderStatus> currentStatuses = getCurrentOrderStatuses(order);

        if (currentStatuses.contains(newStatus)) {
            throw new IllegalArgumentException("Order is already at the requested status: " + newStatus);
        }

        addIntermediateStatuses(order, currentStatuses, newStatus);
    }

    /**
     * Adds intermediate statuses up to the final requested status if required.
     */
    private void addIntermediateStatuses(Order order, List<OrderStatus> currentStatuses, OrderStatus newStatus) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.ordinal() <= newStatus.ordinal() && !currentStatuses.contains(status)) {
                OrderTrack orderTrack = createOrderTrack(order, status, status.getNote());
                order.addOrderTrack(orderTrack);
                orderTrackRepository.save(orderTrack);
            }
        }
    }

    /**
     * Retrieves a list of existing order statuses in the correct order.
     */
    private List<OrderStatus> getCurrentOrderStatuses(Order order) {
        return order.getOrderTracks().stream()
                .map(OrderTrack::getStatus)
                .sorted()
                .toList();
    }

    /**
     * Helper method to create an order track.
     */
    private OrderTrack createOrderTrack(Order order, OrderStatus status, String note) {
        return OrderTrack.builder()
                .updatedTime(LocalDate.now())
                .status(status)
                .notes(note)
                .order(order)
                .build();
    }

    /**
     * Helper method to convert an OrderTrack to a DTO.
     */
    private OrderTrackDTO buildOrderTrackDTO(OrderTrack orderTrack) {
        return OrderTrackDTO.builder()
                .id(orderTrack.getId())
                .orderId(orderTrack.getOrder().getId())
                .note(orderTrack.getNotes())
                .status(orderTrack.getStatus().name())
                .updatedTime(orderTrack.getUpdatedTime())
                .build();
    }

    public List<Order> listAll(String orderTime, Sort.Direction direction) {
        return orderRepository.findAll(Sort.by(direction, orderTime));
    }
}
