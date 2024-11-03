package com.sellsphere.payment.customer;

import com.sellsphere.payment.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.CustomerUpdateParams;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling Stripe customer operations.
 */
@Slf4j
public class StripeCustomerService {

    private final StripeConfig stripeConfig;

    @Inject
    public StripeCustomerService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    /**
     * Creates or updates a Stripe customer based on their email.
     *
     * @param customer The customer details.
     * @return The created or updated Customer object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public Customer createCustomer(Customer customer) throws StripeException {
        try {
            // Find customer by email
            CustomerCollection customerCollection = Customer.list(
                    CustomerListParams.builder()
                            .setEmail(customer.getEmail())
                            .build()
            );

            // If none exist then create new one
            if (customerCollection.getData().isEmpty()) {
                var params = CustomerCreateParams.builder()
                        .setEmail(customer.getEmail())
                        .setName(customer.getName())
                        .build();

                log.info("Creating new customer with email: {}", customer.getEmail());
                return Customer.create(params);
            } else {
                // Update existing customer
                Customer retrievedCustomer = customerCollection.getData().get(0);
                var params = CustomerUpdateParams.builder()
                        .setEmail(customer.getEmail())
                        .setName(customer.getName())
                        .build();

                log.info("Updating existing customer with ID: {}", retrievedCustomer.getId());
                return retrievedCustomer.update(params);
            }
        } catch (StripeException e) {
            log.error("Error creating/updating Stripe customer", e);
            throw e;
        }
    }
}
