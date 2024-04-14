package com.sellsphere.admin;

import com.sellsphere.admin.brand.BrandController;
import com.sellsphere.admin.category.CategoryController;
import com.sellsphere.admin.product.ProductController;
import com.sellsphere.admin.user.UserController;
import com.sellsphere.common.entity.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler class to manage exceptions across the entire application.
 * It captures exceptions thrown by any controller and handles them in a centralized manner,
 * typically by logging the error and redirecting to a specific page with an error message.
 */
@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex,
                                              RedirectAttributes ra) {
        log.warn(ex.getMessage(), ex);

        ra.addFlashAttribute(Constants.ERROR_MESSAGE, ex.getMessage());
        return UserController.DEFAULT_REDIRECT_URL;
    }

    @ExceptionHandler({CategoryNotFoundException.class, CategoryIllegalStateException.class})
    public String handleCategoryRelatedExceptions(Exception ex,
                                              RedirectAttributes ra) {
        log.warn(ex.getMessage(), ex);

        ra.addFlashAttribute(Constants.ERROR_MESSAGE, ex.getMessage());
        return CategoryController.DEFAULT_REDIRECT_URL;
    }

    @ExceptionHandler(BrandNotFoundException.class)
    public String handleBrandNotFoundException(BrandNotFoundException ex,
                                                  RedirectAttributes ra) {
        log.warn(ex.getMessage(), ex);

        ra.addFlashAttribute(Constants.ERROR_MESSAGE, ex.getMessage());
        return BrandController.DEFAULT_REDIRECT_URL;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public String handleProductNotFoundException(ProductNotFoundException ex,
                                               RedirectAttributes ra) {
        log.warn(ex.getMessage(), ex);

        ra.addFlashAttribute(Constants.ERROR_MESSAGE, ex.getMessage());
        return ProductController.DEFAULT_REDIRECT_URL;
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public String handleForbiddenException(NoResourceFoundException ex) {
        log.error(ex.getMessage(), ex);

        return "error/404";
    }

    @ExceptionHandler(ForbiddenException.class)
    public String handleForbiddenException(ForbiddenException ex) {
        log.error(ex.getMessage(), ex);

        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        log.error(ex.getMessage(), ex);

        return "error/500";
    }

}