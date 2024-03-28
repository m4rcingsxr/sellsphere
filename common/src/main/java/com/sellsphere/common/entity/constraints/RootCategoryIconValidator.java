package com.sellsphere.common.entity.constraints;

import com.sellsphere.common.entity.Category;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RootCategoryIconValidator implements
        ConstraintValidator<RootCategoryIconRequired, Category> {

    @Override
    public boolean isValid(Category category, ConstraintValidatorContext context) {
        if (category.getParent() == null) {
            return category.getCategoryIcon() != null;
        }
        return true;
    }
}