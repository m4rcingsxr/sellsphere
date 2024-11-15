package com.sellsphere.client;

import com.sellsphere.client.order.ReturnRequestAlreadyPlacedException;
import com.sellsphere.common.entity.*;
import com.stripe.exception.StripeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice(annotations = RestController.class)
@Order(1)
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler({CustomerNotFoundException.class, CountryNotFoundException.class,
                       StateNotFoundException.class, CurrencyNotFoundException.class, SettingNotFoundException.class,
                       TransactionNotFoundException.class, AddressNotFoundException.class,
                       ExchangeRateNotFoundException.class, OrderNotFoundException.class,
                       WishlistNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(Exception e) {
        log.warn(e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),
                                                        HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ReturnRequestAlreadyPlacedException.class})
    public ResponseEntity<ErrorResponse> handleReturnRequestAlreadyPlacedException(Exception e) {
        log.error(e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),
                                                        HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception e) {
        log.error(e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),
                                                        HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({StripeException.class})
    public ResponseEntity<ErrorResponse> handleStripeException(StripeException e) {
        log.error("{}{}", e.getMessage(), e.getCode(), e);

        Integer statusCode = e.getStatusCode();
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), statusCode);

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(statusCode));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream().collect(
                Collectors.toMap(violation -> (violation).getPropertyPath().toString(),
                                 ConstraintViolation::getMessage,
                                 (message1, message2) -> message1 + "; " + message2
                ));


        return new ResponseEntity<>(
                new ErrorResponse(errors.toString(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
        log.warn(ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Forbidden",
                                                        HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
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