package com.sellsphere.admin.product;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A REST controller for handling product-related operations, specifically checking
 * the uniqueness of a product name.
 */
@RequiredArgsConstructor
@RestController
public class RestProductController {

    private final ProductService productService;

    /**
     * Checks if a product name is unique.
     *
     * This endpoint accepts an optional product ID and a required product name. It checks if the
     * given product name is unique across all products, excluding the product with the given ID
     * (if provided). This is useful for validating product names during creation or updating
     * processes to ensure there are no duplicate names.
     *
     * @param id The ID of the product to exclude from the uniqueness check. This parameter is optional.
     * @param name The name of the product to check for uniqueness. This parameter is required.
     * @return {@code true} if the product name is unique, {@code false} otherwise.
     */
    @PostMapping("/products/check_uniqueness")
    public boolean isProductUnique(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("name") String name
    ) {
        return productService.isNameUnique(id, name);
    }
}