package com.sellsphere.admin.category;

import com.sellsphere.common.entity.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class CategoryRestController {

    private final CategoryService service;

    @PostMapping("/categories/check_alias_uniqueness")
    public ResponseEntity<Boolean> isAliasUnique(
            @RequestParam(value = "id", required = false) Integer categoryId,
            @RequestParam(value = "alias") String alias) {
        if(alias.length() > 64) {
            throw new IllegalArgumentException("Alias length should not exceeds 128 characters");
        }

        boolean isUnique = service.isAliasUnique(categoryId, alias);
        return ResponseEntity.ok(isUnique);
    }

    @PostMapping("/categories/check_name_uniqueness")
    public ResponseEntity<Boolean> isNameUnique(
            @RequestParam(value = "id", required = false) Integer categoryId,
            @RequestParam(value = "name") String name) {
        if(name.length() > 128) {
            throw new IllegalArgumentException("Name length should not exceeds 128 characters");
        }

        boolean isUnique = service.isNameUnique(categoryId, name);
        return ResponseEntity.ok(isUnique);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.",
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}