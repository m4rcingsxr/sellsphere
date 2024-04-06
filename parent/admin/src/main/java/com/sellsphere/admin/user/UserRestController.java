package com.sellsphere.admin.user;

import com.sellsphere.common.entity.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing User-related operations.
 */
@RequiredArgsConstructor
@RestController
public class UserRestController {

    private final UserService service;

    /**
     * Checks the uniqueness of a user email.
     *
     * @param userId the user ID (optional)
     * @param email the user email
     * @return ResponseEntity with a Boolean indicating uniqueness
     */
    @PostMapping("/users/check_uniqueness")
    public ResponseEntity<Boolean> isEmailUnique(@RequestParam(value = "id", required = false) Integer userId,
                                                 @RequestParam("email") String email) {
        if(email.length() > 128) {
            throw new IllegalArgumentException("Email should not exceed 128 characters");
        }

        boolean isUnique = service.isEmailUnique(userId, email);
        return ResponseEntity.ok(isUnique);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
