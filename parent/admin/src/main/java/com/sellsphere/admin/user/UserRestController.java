package com.sellsphere.admin.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

}
