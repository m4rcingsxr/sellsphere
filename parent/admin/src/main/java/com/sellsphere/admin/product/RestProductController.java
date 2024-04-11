package com.sellsphere.admin.product;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RestProductController {

    private final ProductService productService;

    @PostMapping("/products/check_uniqueness")
    public boolean isProductUnique(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("name") String name
    ) {
        return productService.isNameUnique(id, name);
    }

}
