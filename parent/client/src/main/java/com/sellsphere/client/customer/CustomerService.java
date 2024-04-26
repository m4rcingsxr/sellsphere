package com.sellsphere.client.customer;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Retrieve a customer entity by its email.
     *
     * @param email The email of the customer
     * @return The customer entity
     * @throws CustomerNotFoundException If the customer is not found
     */
    public Customer getByEmail(String email) throws CustomerNotFoundException {
        return customerRepository.findByEmail(email).orElseThrow(
                CustomerNotFoundException::new);
    }

}
