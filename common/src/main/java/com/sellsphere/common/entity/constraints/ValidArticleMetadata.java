package com.sellsphere.common.entity.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ArticleMetadataValidator.class)
@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidArticleMetadata {
    String message() default "Invalid article metadata";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
