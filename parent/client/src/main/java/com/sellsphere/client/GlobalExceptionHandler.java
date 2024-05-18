package com.sellsphere.client;

import com.sellsphere.client.address.AddressController;
import com.sellsphere.common.entity.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler class to manage exceptions across the entire application.
 * It captures exceptions thrown by any controller and handles them in a centralized manner,
 * typically by logging the error and redirecting to a specific page with an error message.
 */
@Log4j2
@ControllerAdvice(annotations = Controller.class)
@Order(2)
public class GlobalExceptionHandler {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public String handleCustomerNotFound(CustomerNotFoundException ex, RedirectAttributes ra) {
        log.error(ex.getMessage(), ex);

        ra.addFlashAttribute(Constants.ERROR_MESSAGE, ex.getMessage());

        return "redirect:/";
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public String handleAddressNotFound(AddressNotFoundException ex, RedirectAttributes ra) {
        log.error(ex.getMessage(), ex);

        ra.addFlashAttribute(Constants.ERROR_MESSAGE, ex.getMessage());

        return AddressController.ADDRESS_BOOK_DEFAULT_REDIRECT_URL;
    }

    @ExceptionHandler({NoResourceFoundException.class, ProductNotFoundException.class})
    public String handleForbiddenException(Exception ex) {
        log.error(ex.getMessage(), ex);

        return "error/404";
    }

    @ExceptionHandler(ForbiddenException.class)
    public String handleForbiddenException(ForbiddenException ex, HttpServletResponse response) {
        response.setStatus(403);
        log.error(ex.getMessage(), ex);

        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        log.error(ex.getMessage(), ex);

        return "error/500";
    }

}