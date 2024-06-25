package com.sellsphere.admin.order;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.OrderTrackDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Integer ORDERS_PER_PAGE = 10;

    private final OrderRepository repository;
    private final OrderTrackRepository orderTrackRepository;

    /**
     * Lists orders by page, with optional keyword filtering, sorting, and
     * pagination.
     *
     * @param page the page number to retrieve.
     * @param helper  a helper object containing pagination and sorting
     *                parameters.
     */
    public void listPage(Integer page, PagingAndSortingHelper helper) {
        helper.listEntities(page, ORDERS_PER_PAGE, repository);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the ID of the order to retrieve.
     * @return the found order.
     * @throws OrderNotFoundException if no order is found with the given ID.
     */
    public Order getById(Integer orderId) throws OrderNotFoundException {
        return repository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    public OrderTrackDTO addOrderTrack(Integer orderId, OrderStatus orderStatus, String note)
            throws OrderNotFoundException {
        // verify whether this track already has been uploaded - if it does then inform

        if (isOrderTrackUnique(orderId, orderStatus)) {
            Order order = getById(orderId);

            boolean isCorrectOrder = isOrderTrackInCorrectOrder(
                    order.getOrderTracks(),
                    orderStatus
            );

            if (isCorrectOrder) {
                OrderTrack orderTrack = orderTrackRepository.save(
                        OrderTrack.builder()
                                .updatedTime(LocalDate.now())
                                .status(orderStatus)
                                .notes(note)
                                .order(order)
                                .build()
                );

                return OrderTrackDTO.builder()
                        .updateTime(orderTrack.getUpdatedTime())
                        .id(orderTrack.getId())
                        .orderId(orderId)
                        .note(orderTrack.getNotes())
                        .status(orderTrack.getStatus().name())
                        .build();

            } else {
                throw new IllegalArgumentException(
                        "Order track is not in correct order. " + orderStatus.name() + " cannot " +
                                "appear after " + order.getOrderTracks().get(
                                order.getOrderTracks().size() - 1));
            }


        } else {
            throw new IllegalArgumentException("Order track already exist in tracking");
        }

    }

    public boolean isOrderTrackUnique(Integer orderId, OrderStatus orderStatus) {
        Optional<OrderTrack> orderTrack = orderTrackRepository
                .findByOrderIdAndStatus(orderId, orderStatus);

        return orderTrack.isEmpty();
    }

    public boolean isOrderTrackInCorrectOrder(List<OrderTrack> orderTracks, OrderStatus status) {
        List<OrderTrack> tracks = orderTracks.stream()
                .sorted(Comparator.comparingInt(x -> x.getStatus().ordinal()))
                .toList();
        if (!tracks.isEmpty()) {
            OrderTrack orderTrack = tracks.get(tracks.size() - 1);
            return orderTrack.getStatus().ordinal() <= status.ordinal();
        }
        return true;
    }

    public List<OrderTrackDTO> listOrderTracks(Integer orderId) {
        List<OrderTrack> tracks = orderTrackRepository.findByOrderId(orderId);
        return tracks.stream()
                .sorted(Comparator.comparingInt(x -> x.getStatus().ordinal()))
                .map(track ->
                             OrderTrackDTO.builder()
                                     .id(track.getId())
                                     .orderId(orderId)
                                     .note(track.getNotes())
                                     .status(track.getStatus().name())
                                     .updateTime(track.getUpdatedTime())
                                     .build()
                )
                .toList();
    }

    public OrderTrackDTO deleteTrack(Integer id) throws OrderTrackNotFoundException {
        OrderTrack orderTrack = orderTrackRepository
                .findById(id)
                .orElseThrow(OrderTrackNotFoundException::new);
        orderTrackRepository.delete(orderTrack);

        return OrderTrackDTO.builder()
                .id(orderTrack.getId())
                .updateTime(orderTrack.getUpdatedTime())
                .status(orderTrack.getStatus().name())
                .note(orderTrack.getNotes())
                .build();
    }
}
