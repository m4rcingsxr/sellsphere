package com.sellsphere.client.product;

import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class RequestValidatorUtils {

    public static boolean validateCategoryAndKeyword(String categoryAlias, String keyword, ConstraintValidatorContext context) {
        boolean valid = true;

        if ((categoryAlias == null || categoryAlias.isEmpty()) && (keyword == null || keyword.isEmpty())) {
            context.buildConstraintViolationWithTemplate("Either categoryAlias or keyword must be provided")
                    .addPropertyNode("categoryAlias")
                    .addPropertyNode("keyword")
                    .addConstraintViolation();
            valid = false;
        }

        if (categoryAlias != null && !categoryAlias.isEmpty() && keyword != null && !keyword.isEmpty()) {
            context.buildConstraintViolationWithTemplate("Only one of categoryAlias or keyword must be provided")
                    .addPropertyNode("categoryAlias")
                    .addPropertyNode("keyword")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }

    public static boolean validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice, ConstraintValidatorContext context) {
        boolean valid = true;

        if ((minPrice == null && maxPrice != null) || (minPrice != null && maxPrice == null)) {
            context.buildConstraintViolationWithTemplate("Both minPrice and maxPrice must be provided together or both must be null")
                    .addPropertyNode("minPrice")
                    .addPropertyNode("maxPrice")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }

}
