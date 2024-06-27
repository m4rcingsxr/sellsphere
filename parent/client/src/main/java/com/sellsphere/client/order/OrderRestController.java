package com.sellsphere.client.order;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class OrderRestController {

    private final OrderService orderService;
    private final CustomerService customerService;

    /**
     * Handles order return requests.
     *
     * @param returnRequest the return request details
     * @param principal     the authenticated user's principal
     * @return ResponseEntity with either a success response or an error message
     */
    @PostMapping("/orders/return")
    public ResponseEntity<OrderReturnResponse> handleOrderReturnRequest(
            @RequestBody OrderReturnRequest returnRequest,
            Principal principal)
            throws CustomerNotFoundException, OrderNotFoundException,
            ReturnRequestAlreadyPlacedException {
        Customer customer = getAuthenticatedCustomer(principal);

        orderService.setOrderReturnRequested(returnRequest, customer);
        return new ResponseEntity<>(new OrderReturnResponse(returnRequest.getOrderId()), HttpStatus.OK);
    }

    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        String email = principal.getName();
        return customerService.getByEmail(email);
    }
}
