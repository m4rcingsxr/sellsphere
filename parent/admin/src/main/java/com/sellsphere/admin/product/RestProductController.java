package com.sellsphere.admin.product;

import com.sellsphere.common.entity.ProductTax;
import com.sellsphere.common.entity.TaxType;
import com.sellsphere.easyship.EasyshipIntegrationService;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.easyship.payload.HsCode;
import com.sellsphere.easyship.payload.HsCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A REST controller for handling product-related operations, specifically checking
 * the uniqueness of a product name.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class RestProductController {

    private final ProductService productService;
    private final ProductTaxRepository productTaxRepository;
    private final EasyshipService easyshipService;

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
    @PostMapping("/check_uniqueness")
    public boolean isProductUnique(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("name") String name
    ) {
        return productService.isNameUnique(id, name);
    }

    @GetMapping("/tax/{type}")
    public ResponseEntity<List<ProductTax>> findByTaxType(@PathVariable("type") String type) {
        TaxType taxType = TaxType.valueOf(type);

        List<ProductTax> taxes = productTaxRepository.findByType(taxType);

        return ResponseEntity.ok(taxes);
    }

    @GetMapping("/hs-codes")
    public ResponseEntity<HsCodeResponse> listHsCodes(
            @RequestParam("page") Integer page,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "description", required = false) String description
    ) {
        HsCodeResponse response = easyshipService.fetchHsCodes(page, code, description);
        return ResponseEntity.ok(response);
    }

}