package com.sellsphere.client.customer;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * Update the details of a customer.
     * The password is retained from the existing customer data.
     *
     * @param newCustomer The updated customer entity
     * @return The updated customer entity
     * @throws CustomerNotFoundException If the customer is not found
     */
    public Customer update(Customer newCustomer)
            throws CustomerNotFoundException {

        Customer existingCustomer = getByEmail(newCustomer.getEmail());
        existingCustomer.setFirstName(newCustomer.getFirstName());
        existingCustomer.setLastName(newCustomer.getLastName());
        existingCustomer.setEmail(newCustomer.getEmail());

        if(newCustomer.getPassword() != null) {
            existingCustomer.setPassword(passwordEncoder.encode(newCustomer.getPassword()));
        }

        return customerRepository.save(existingCustomer);
    }

    public Customer getById(Integer id) throws CustomerNotFoundException {
        return customerRepository.findById(id).orElseThrow(CustomerNotFoundException::new);
    }
}
