package com.sellsphere.client.product;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FilterMapCountRequestValidator implements ConstraintValidator<ValidFilterRequest, FilterMapCountRequest> {

    @Override
    public void initialize(ValidFilterRequest constraintAnnotation) {
    }

    @Override
    public boolean isValid(FilterMapCountRequest value, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        valid = RequestValidatorUtils.validatePriceRange(value.getMinPrice(), value.getMaxPrice(), context);

        return valid;
    }
}
