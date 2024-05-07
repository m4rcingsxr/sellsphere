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

        // Implement the necessary validation logic here, similar to the logic in ProductPageRequestValidator

        if ((value.getCategoryAlias() == null || value.getCategoryAlias().isEmpty())
                && (value.getKeyword() == null || value.getKeyword().isEmpty())) {
            context.buildConstraintViolationWithTemplate("Either categoryAlias or keyword must be provided")
                    .addPropertyNode("categoryAlias")
                    .addPropertyNode("keyword")
                    .addConstraintViolation();
            valid = false;
        }

        if (value.getCategoryAlias() != null && !value.getCategoryAlias().isEmpty()
                && value.getKeyword() != null && !value.getKeyword().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Only one of categoryAlias or keyword must be provided")
                    .addPropertyNode("categoryAlias")
                    .addPropertyNode("keyword")
                    .addConstraintViolation();
            valid = false;
        }

        if ((value.getMinPrice() == null && value.getMaxPrice() != null)
                || (value.getMinPrice() != null && value.getMaxPrice() == null)) {
            context.buildConstraintViolationWithTemplate("Both minPrice and maxPrice must be provided together or both must be null")
                    .addPropertyNode("minPrice")
                    .addPropertyNode("maxPrice")
                    .addConstraintViolation();
            valid = false;
        }

        context.disableDefaultConstraintViolation();
        return valid;
    }
}