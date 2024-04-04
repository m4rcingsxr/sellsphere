package com.sellsphere.admin.brand;

import com.sellsphere.common.entity.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class BrandRestController {

    private final BrandService service;

    @PostMapping("/brands/check_uniqueness")
    public ResponseEntity<Boolean> isAliasUnique(
            @RequestParam(value = "id", required = false) Integer brandId,
            @RequestParam(value = "name") String alias) {
        if(alias.length() > 45) {
            throw new IllegalArgumentException("Brand name length should not exceeds 45 characters");
        }

        boolean isUnique = service.isNameUnique(brandId, alias);
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
