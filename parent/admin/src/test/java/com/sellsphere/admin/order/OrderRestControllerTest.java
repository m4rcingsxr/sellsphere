package com.sellsphere.admin.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellsphere.common.entity.OrderNotFoundException;
import com.sellsphere.common.entity.OrderStatus;
import com.sellsphere.common.entity.OrderTrackNotFoundException;
import com.sellsphere.common.entity.payload.OrderTrackDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrderRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenOrderId_whenGetOrderTracks_thenReturnOrderTracks() throws Exception {
        // Given
        List<OrderTrackDTO> tracks = List.of(
                OrderTrackDTO.builder().id(1).status("NEW").note("New order created").build()
        );
        given(orderService.listOrderTracks(1)).willReturn(tracks);

        // When / Then
        mockMvc.perform(get("/orders/order-tracks")
                        .param("orderId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("NEW"));

        then(orderService).should().listOrderTracks(1);
    }

    @Test
    void givenValidTrackId_whenRemoveOrderTrack_thenReturnDeletedTrack() throws Exception {
        // Given
        OrderTrackDTO deletedTrack = OrderTrackDTO.builder().id(1).status("NEW").note("Order track deleted").build();
        given(orderService.deleteTrack(1)).willReturn(deletedTrack);

        // When / Then
        mockMvc.perform(delete("/orders/remove-track")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.note").value("Order track deleted"));

        then(orderService).should().deleteTrack(1);
    }

    @Test
    void givenInvalidTrackId_whenRemoveOrderTrack_thenThrowOrderTrackNotFoundException() throws Exception {
        // Given
        given(orderService.deleteTrack(999)).willThrow(new OrderTrackNotFoundException());

        // When / Then
        mockMvc.perform(delete("/orders/remove-track")
                        .param("id", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(orderService).should().deleteTrack(999);
    }

    @Test
    void givenValidOrderTrack_whenAddOrderTrack_thenReturnAddedTrack() throws Exception {
        // Given
        OrderTrackDTO orderTrackDTO = OrderTrackDTO.builder().orderId(1).status("PACKAGED").note("Packaging order").build();
        OrderTrackDTO addedTrack = OrderTrackDTO.builder().id(1).orderId(1).status("PACKAGED").note("Packaging order").build();
        given(orderService.addOrderTrack(anyInt(), any(OrderStatus.class), any(String.class))).willReturn(addedTrack);

        // When / Then
        mockMvc.perform(post("/orders/add-track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderTrackDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PACKAGED"))
                .andExpect(jsonPath("$.note").value("Packaging order"));

        then(orderService).should().addOrderTrack(1, OrderStatus.PACKAGED, "Packaging order");
    }

    @Test
    void givenInvalidOrderId_whenAddOrderTrack_thenThrowOrderNotFoundException() throws Exception {
        // Given
        OrderTrackDTO orderTrackDTO = OrderTrackDTO.builder().orderId(999).status("PACKAGED").note("Packaging order").build();
        given(orderService.addOrderTrack(anyInt(), any(OrderStatus.class), any(String.class)))
                .willThrow(new OrderNotFoundException());

        // When / Then
        mockMvc.perform(post("/orders/add-track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderTrackDTO)))
                .andExpect(status().isNotFound());

        then(orderService).should().addOrderTrack(999, OrderStatus.PACKAGED, "Packaging order");
    }

}
