package com.sellsphere.client.product;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductPageRequestValidator implements ConstraintValidator<ValidFilterRequest, ProductPageRequest> {

    @Override
    public void initialize(ValidFilterRequest constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(ProductPageRequest value, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        valid &= RequestValidatorUtils.validateCategoryAndKeyword(value.getCategoryAlias(), value.getKeyword(), context);
        valid &= RequestValidatorUtils.validatePriceRange(value.getMinPrice(), value.getMaxPrice(), context);

        return valid;
    }
}
