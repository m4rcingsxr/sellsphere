package com.sellsphere.client.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrderRestControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenValidReturnRequest_whenProcessed_thenReturnSuccessResponse() throws Exception {
        // Given
        OrderReturnRequest returnRequest = new OrderReturnRequest(1, "Defective item", "Customer notes");
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();

        given(customerService.getByEmail(principal.getName())).willReturn(customer);

        // When & Then
        mockMvc.perform(post("/orders/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(returnRequest))
                                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void givenNonExistentCustomer_whenProcessed_thenReturnCustomerNotFoundError() throws Exception {
        // Given
        OrderReturnRequest returnRequest = new OrderReturnRequest(1, "Defective item", "Customer notes");
        Principal principal = () -> "nonexistent@example.com";

        willThrow(new CustomerNotFoundException("Customer not found"))
                .given(customerService).getByEmail(principal.getName());

        // When & Then
        mockMvc.perform(post("/orders/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(returnRequest))
                                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void givenExistingReturnRequest_whenProcessed_thenReturnConflictError() throws Exception {
        // Given
        OrderReturnRequest returnRequest = new OrderReturnRequest(1, "Defective item", "Customer notes");
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();

        given(customerService.getByEmail(principal.getName())).willReturn(customer);
        willThrow(new ReturnRequestAlreadyPlacedException("Return request already placed")).given(orderService).setOrderReturnRequested(any(OrderReturnRequest.class), any(Customer.class));

        // When & Then
        mockMvc.perform(post("/orders/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(returnRequest))
                                .principal(principal))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Return request already placed"));
    }

    @Test
    void givenNonExistentOrder_whenProcessed_thenReturnOrderNotFoundError() throws Exception {
        // Given
        OrderReturnRequest returnRequest = new OrderReturnRequest(1, "Defective item", "Customer notes");
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();

        given(customerService.getByEmail(principal.getName())).willReturn(customer);
        willThrow(new OrderNotFoundException("Order not found"))
                .given(orderService).setOrderReturnRequested(any(OrderReturnRequest.class), any(Customer.class));

        // When & Then
        mockMvc.perform(post("/orders/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(returnRequest))
                                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found"));
    }
}
