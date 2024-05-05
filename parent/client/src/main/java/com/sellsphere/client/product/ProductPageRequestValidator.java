package com.sellsphere.client.product;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductPageRequestValidator implements
        ConstraintValidator<ValidFilterRequest, ProductPageRequest> {

    @Override
    public boolean isValid(ProductPageRequest value, ConstraintValidatorContext context) {
        boolean valid = true;

        // Check if either categoryAlias or keyword is provided but not both
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

        // Check if both minPrice and maxPrice are either null or both are provided
        if ((value.getMinPrice() == null && value.getMaxPrice() != null)
                || (value.getMinPrice() != null && value.getMaxPrice() == null)) {
            context.buildConstraintViolationWithTemplate("Both minPrice and maxPrice must be provided together or both must be null")
                    .addPropertyNode("minPrice")
                    .addPropertyNode("maxPrice")
                    .addConstraintViolation();
            valid = false;
        }

        // Disable the default constraint violation
        context.disableDefaultConstraintViolation();

        return valid;
    }
}