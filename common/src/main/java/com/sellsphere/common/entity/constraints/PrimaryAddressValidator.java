package com.sellsphere.common.entity.constraints;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Customer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PrimaryAddressValidator implements ConstraintValidator<ValidPrimaryAddress, Customer> {

    @Override
    public boolean isValid(Customer customer, ConstraintValidatorContext context) {
        if (customer == null || customer.getAddresses() == null || customer.getAddresses().isEmpty()) {
            return false; // No addresses should be considered invalid as one primary address is required
        }

        long primaryCount = customer.getAddresses().stream()
                .filter(Address::isPrimary)
                .count();

        if (primaryCount != 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("addresses")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}