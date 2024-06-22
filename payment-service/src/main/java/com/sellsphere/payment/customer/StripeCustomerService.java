package com.sellsphere.payment.customer;

import com.sellsphere.payment.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.CustomerUpdateParams;

/**
 * Service for handling Stripe customer operations.
 * This class provides methods to create and update Stripe customers.
 */
public class StripeCustomerService {

    static {
        StripeConfig.init();
    }

    /**
     * Creates or updates a Stripe customer based on their email.
     *
     * @param customer The customer details.
     * @return The created or updated Customer object.
     * @throws StripeException if there is an error with Stripe operations.
     */
    public Customer createCustomer(Customer customer) throws StripeException {

        // find customer by email
        CustomerCollection customerCollection = Customer.list(
                CustomerListParams.builder()
                        .setEmail(customer.getEmail())
                        .build()
        );

        // if none exist then create new one
        if(customerCollection.getData().isEmpty()) {
            var params = CustomerCreateParams.builder()
                    .setEmail(customer.getEmail())
                    .setName(customer.getName())
                    .build();

            return Customer.create(params);
        } else {

            // update existing customer
            Customer retrievedCustomer = customerCollection.getData().get(0);
            var params = CustomerUpdateParams.builder()
                    .setEmail(customer.getEmail())
                    .setName(customer.getName())
                    .build();

            return retrievedCustomer.update(params);
        }
    }

}
