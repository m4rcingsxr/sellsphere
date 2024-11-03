package com.sellsphere.admin.customer;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingHelper;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This service class encapsulates business logic for managing Customer
 * entities. It provides functionality to create, retrieve, update, and
 * delete customers from the repository. It also includes pagination and
 * sorting capabilities for listing customers. Passwords are securely managed
 * by encoding new passwords and retaining existing ones during updates. The
 * class ensures that email uniqueness and customer enablement status are
 * properly handled.
 */
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public static final int CUSTOMERS_PER_PAGE = 10;

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return The requested Customer object.
     * @throws CustomerNotFoundException If no customer with the given ID is found.
     */
    public Customer get(Integer id) throws CustomerNotFoundException {
        return customerRepository
                .findById(id)
                .orElseThrow(CustomerNotFoundException::new);
    }

    public List<Customer> listAll() {
        return customerRepository.findAll();
    }

    /**
     * Lists all customers sorted by the specified field and direction.
     *
     * @param sortField The field by which to sort the list.
     * @param sortDir The direction of the sort (ascending or descending).
     * @return A list of all customers, sorted according to the specified parameters.
     */
    public List<Customer> listAll(String sortField, Sort.Direction sortDir) {
        return customerRepository.findAll(PagingHelper.getSort(sortField, sortDir));
    }

    /**
     * Determines if an email is unique within the system. Useful for ensuring that
     * no two customers have the same email address.
     *
     * @param customerId The ID of the customer being checked (to exclude their own email).
     * @param email The email address to check for uniqueness.
     * @return true if the email is unique, or false otherwise.
     */
    public boolean isEmailUnique(Integer customerId, String email) {
        return customerRepository.findByEmail(email)
                .map(customer -> customer.getId().equals(customerId))
                .orElse(true);
    }

    /**
     * Lists customers by page, with support for sorting, search keyword and pagination.
     *
     * @param pageNum The page number to retrieve.
     * @param helper A helper object for managing pagination and sorting.
     */
    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, CUSTOMERS_PER_PAGE, customerRepository);
    }

    /**
     * Saves a customer to the repository. If the customer is new, their password is encoded.
     * If the customer is being updated and no new password is provided, the existing password is retained.
     *
     * @param customer The customer to save.
     * @throws CustomerNotFoundException If the customer to be updated does not exist.
     */
    public void save(Customer customer) throws CustomerNotFoundException {
        if (Objects.isNull(customer.getPassword())) {
            setExistingPassword(customer);
        } else {
            encodePassword(customer);
        }

        customerRepository.save(customer);
    }

    private void setExistingPassword(Customer customer)
            throws CustomerNotFoundException {
        Customer customerById = customerRepository
                .findById(customer.getId())
                .orElseThrow(CustomerNotFoundException::new);

        customer.setPassword(customerById.getPassword());
    }

    private void encodePassword(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
    }

    /**
     * Deletes a customer by their ID.
     *
     * @param id The ID of the customer to delete.
     * @throws CustomerNotFoundException If no customer with the given ID is found.
     */
    public void delete(Integer id)
            throws CustomerNotFoundException {

        Optional<Customer> customerById = customerRepository.findById(id);
        Customer customer = customerById.orElseThrow(
                CustomerNotFoundException::new);

        customerRepository.delete(customer);
    }

    /**
     * Updates the enabled status of a customer.
     *
     * @param id The ID of the customer whose status is to be updated.
     * @param enabled The new enabled status for the customer.
     * @throws CustomerNotFoundException If no customer with the given ID is found.
     */
    public void updateCustomerEnabledStatus(Integer id, boolean enabled)
            throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(id).orElseThrow(
                CustomerNotFoundException::new);
        customer.setEnabled(enabled);
        customerRepository.save(customer);
    }


}
