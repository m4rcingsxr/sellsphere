package com.sellsphere.client.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/products/filter")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    @GetMapping("/page")
    public ResponseEntity<ProductPageResponse> pageFilteredProducts(
            @ProductFilter ProductPageRequest pageRequest
    ) {
        if (pageRequest.getCategoryAlias() == null && pageRequest.getKeyword() == null) {
            throw new IllegalArgumentException(
                    "Category or Keyword query parameter is required to consume this endpoint.");
        }

        ProductPageResponse page = productService.listProductsPage(pageRequest);

        return ResponseEntity.ok(page);
    }


}
