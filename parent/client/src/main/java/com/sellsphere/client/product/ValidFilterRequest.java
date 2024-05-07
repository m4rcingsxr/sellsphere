package com.sellsphere.client.product;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ProductPageRequestValidator.class, FilterMapCountRequestValidator.class})
public @interface ValidFilterRequest {
    String message() default "Invalid product page request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}