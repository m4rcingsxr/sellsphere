package com.sellsphere.admin.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserRestController {

    private final UserService service;

    @PostMapping("/users/check_uniqueness")
    public ResponseEntity<?> isEmailUnique(
            @RequestParam(value = "id", required = false) Integer userId,
            @RequestParam("email") String email) {
        try {
            boolean isUnique = service.isEmailUnique(userId, email);
            return ResponseEntity.ok(isUnique);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while checking email uniqueness");
        }
    }

}
