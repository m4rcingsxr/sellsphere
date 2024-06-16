package com.sellsphere.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.CustomerUpdateParams;

import java.util.List;

public class CustomerService {

    static {
        Stripe.apiKey = "sk_test_51PbnPoAx8ZpOoq6Yma531EHxQhtmlY717PYSKoRPSRe6So8e1stJkyNfOmVEZP6eDuxJygev23JWI4b5chR88Kuy00O8BD03Xz";
    }

    public Customer createCustomer(Customer customer) throws StripeException {
        CustomerCollection customerCollection = Customer.list(
                CustomerListParams.builder()
                        .setEmail(customer.getEmail())
                        .build()
        );

        if(customerCollection.getData().isEmpty()) {
            var params = CustomerCreateParams.builder()
                    .setEmail(customer.getEmail())
                    .setName(customer.getName())
                    .build();

            return Customer.create(params);
        } else {
            Customer retrievedCustomer = customerCollection.getData().get(0);
            var params = CustomerUpdateParams.builder()
                    .setEmail(customer.getEmail())
                    .setName(customer.getName())
                    .build();

            return retrievedCustomer.update(params);
        }
    }

}
