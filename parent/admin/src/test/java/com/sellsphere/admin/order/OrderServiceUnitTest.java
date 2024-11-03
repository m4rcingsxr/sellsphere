package com.sellsphere.admin.order;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.OrderTrackDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTrackRepository orderTrackRepository;

    @Mock
    private PagingAndSortingHelper helper;

    private Order order;
    private OrderTrack orderTrack;

    @BeforeEach
    void setup() {
        order = Order.builder()
                .orderTime(LocalDateTime.now())
                .orderTracks(List.of())
                .build();
        order.setId(1);

        orderTrack = OrderTrack.builder()
                .status(OrderStatus.NEW)
                .updatedTime(LocalDate.now())
                .order(order)
                .build();
        orderTrack.setId(1);
    }

    @Test
    void givenPageRequest_whenListPage_thenHelperListsEntities() {
        // Given
        int page = 0;

        // When
        orderService.listPage(page, helper);

        // Then
        then(helper).should().listEntities(page, 10, orderRepository);
    }

    @Test
    void givenValidOrderId_whenGetById_thenReturnOrder() throws OrderNotFoundException {
        // Given
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        // When
        Order foundOrder = orderService.getById(order.getId());

        // Then
        assertEquals(order.getId(), foundOrder.getId());
    }

    @Test
    void givenNonexistentOrderId_whenGetById_thenThrowOrderNotFoundException() {
        // Given
        int nonexistentOrderId = 999;
        given(orderRepository.findById(nonexistentOrderId)).willReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> orderService.getById(nonexistentOrderId));
    }

    @Test
    void givenOrderIdAndNewTrack_whenAddOrderTrack_thenTrackIsAdded() throws OrderNotFoundException {
        // Given
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
        given(orderTrackRepository.findByOrderIdAndStatus(order.getId(), OrderStatus.PACKAGED)).willReturn(Optional.empty());

        // When
        OrderTrackDTO dto = orderService.addOrderTrack(order.getId(), OrderStatus.PACKAGED, "Packaged order");

        // Then
        assertNotNull(dto);
        assertEquals(OrderStatus.PACKAGED.name(), dto.getStatus());
        assertEquals("Packaged order", dto.getNote());
        then(orderTrackRepository).should().save(any(OrderTrack.class));
    }

    @Test
    void givenExistingTrackStatus_whenAddOrderTrack_thenThrowException() {
        // Given
        given(orderTrackRepository.findByOrderIdAndStatus(order.getId(), OrderStatus.NEW)).willReturn(Optional.of(orderTrack));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> orderService.addOrderTrack(order.getId(), OrderStatus.NEW, "Already exists"));
    }

    @Test
    void givenOrderId_whenDeleteTrack_thenTrackIsDeleted() throws OrderTrackNotFoundException {
        // Given
        given(orderTrackRepository.findById(orderTrack.getId())).willReturn(Optional.of(orderTrack));

        // When
        OrderTrackDTO dto = orderService.deleteTrack(orderTrack.getId());

        // Then
        assertEquals(orderTrack.getId(), dto.getId());
        then(orderTrackRepository).should().delete(orderTrack);
    }

    @Test
    void givenNonexistentTrackId_whenDeleteTrack_thenThrowException() {
        // Given
        int nonexistentTrackId = 999;
        given(orderTrackRepository.findById(nonexistentTrackId)).willReturn(Optional.empty());

        // When / Then
        assertThrows(OrderTrackNotFoundException.class, () -> orderService.deleteTrack(nonexistentTrackId));
    }

    @Test
    void givenExistingFinalStatus_whenSetOrderStatus_thenThrowException() {
        // Given
        order.setOrderTracks(List.of(orderTrack));
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> orderService.setOrderStatus(order.getId(), OrderStatus.NEW));
    }

    @Test
    void givenOrderId_whenListOrderTracks_thenReturnSortedOrderTracks() {
        // Given
        OrderTrack track1 = OrderTrack.builder().status(OrderStatus.PAID).order(order).updatedTime(LocalDate.now()).build();
        track1.setId(1);
        OrderTrack track2 = OrderTrack.builder().status(OrderStatus.PROCESSING).order(order).updatedTime(LocalDate.now()).build();
        track2.setId(2);
        given(orderTrackRepository.findByOrderId(order.getId())).willReturn(List.of(track2, track1));

        // When
        List<OrderTrackDTO> result = orderService.listOrderTracks(order.getId());

        // Then
        assertEquals(2, result.size());
        assertEquals(OrderStatus.PAID.name(), result.get(0).getStatus());
        assertEquals(OrderStatus.PROCESSING.name(), result.get(1).getStatus());
    }
}
