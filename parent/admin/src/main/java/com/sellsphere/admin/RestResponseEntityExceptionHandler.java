package com.sellsphere.admin;

import com.sellsphere.common.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations = RestController.class)
@Order(1)
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler({BrandNotFoundException.class, CountryNotFoundException.class,
                       StateNotFoundException.class, CartItemNotFoundException.class,
                       OrderNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),
                                                        HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),
                                                        HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.",
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}