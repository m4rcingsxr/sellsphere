package com.sellsphere.admin.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerService customerService;

    @PostMapping("/customers/check_uniqueness")
    public ResponseEntity<Boolean> isEmailUnique(@RequestParam("email") String email, @RequestParam("id") Integer id) {
        return ResponseEntity.ok(customerService.isEmailUnique(id, email));
    }


}