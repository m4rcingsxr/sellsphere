package com.sellsphere.common.entity.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RootCategoryIconValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RootCategoryIconRequired {
    String message() default "Root category must have an icon";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}