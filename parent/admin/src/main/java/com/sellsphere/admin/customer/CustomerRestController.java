package com.sellsphere.admin.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling customer-related API operations.
 * This controller is used to check the uniqueness of a customer's email during creation or update.
 */
@RestController
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerService customerService;

    /**
     * Checks if the provided email is unique for a customer.
     *
     * @param email the email address to check for uniqueness
     * @param id    the customer ID (used in update scenarios to exclude the current customer from the uniqueness check)
     * @return a ResponseEntity containing a Boolean value indicating whether the email is unique or not
     */
    @PostMapping("/customers/check-uniqueness")
    public ResponseEntity<Boolean> checkEmailUniqueness(@RequestParam("email") String email, @RequestParam(value = "id", required = false) Integer id) {
        boolean isUnique = customerService.isEmailUnique(id, email);
        return ResponseEntity.ok(isUnique);
    }

}