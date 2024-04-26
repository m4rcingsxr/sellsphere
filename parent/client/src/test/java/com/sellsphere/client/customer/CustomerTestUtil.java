package com.sellsphere.client.customer;

import com.sellsphere.common.entity.Customer;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class CustomerTestUtil {

    public static Customer generateDummyCustomer() {
        Customer customer = new Customer();
        customer.setEmail("example@gmail.com");
        customer.setFirstName("First name");
        customer.setLastName("Last name");
        customer.setPassword("Password123");
        customer.setCreatedTime(LocalDateTime.now());

        return customer;
    }

}
