package com.sellsphere.admin.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling User-related operations, including checking the uniqueness of user emails.
 */
@RequiredArgsConstructor
@RestController
public class UserRestController {

    private static final int MAX_EMAIL_LENGTH = 128;

    private final UserService service;

    /**
     * Checks if a user email is unique within the system.
     *
     * @param userId the user ID (optional, used for update scenarios).
     * @param email the email to check for uniqueness.
     * @return ResponseEntity with a Boolean indicating whether the email is unique.
     */
    @PostMapping("/users/email/unique-check")
    public ResponseEntity<Boolean> checkEmailUniqueness(@RequestParam(value = "id", required = false) Integer userId,
                                                        @RequestParam("email") String email) {
        // Validate email length
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email length should not exceed " + MAX_EMAIL_LENGTH + " characters");
        }

        boolean isUnique = service.isEmailUnique(userId, email);
        return ResponseEntity.ok(isUnique);
    }

}