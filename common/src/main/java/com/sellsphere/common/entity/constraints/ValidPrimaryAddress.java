package com.sellsphere.common.entity.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PrimaryAddressValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrimaryAddress {
    String message() default "At least one address must be marked as primary";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}