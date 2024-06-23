package com.sellsphere.common.entity.constraints;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Customer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PrimaryAddressValidator implements ConstraintValidator<ValidPrimaryAddress, Customer> {

    @Override
    public boolean isValid(Customer customer, ConstraintValidatorContext context) {
        if (customer == null || customer.getAddresses() == null) {
            return true; // Valid because customer is null or addresses list is null, indicating no addresses.
        }

        long primaryCount = customer.getAddresses().stream()
                .filter(Address::isPrimary)
                .count();

        if (primaryCount != 1 && !customer.getAddresses().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("addresses")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}