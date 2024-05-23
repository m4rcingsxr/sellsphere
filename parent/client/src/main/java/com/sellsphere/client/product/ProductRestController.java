package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class,
                                        new StringTrimmerEditor(true)
        );
    }

    /**
     * Retrieves a list of products based on their IDs.
     *
     * @param productIds the IDs of the products to retrieve
     * @return a list of ProductDTO objects
     */
    @PostMapping("/products")
    public ResponseEntity<List<BasicProductDto>> getProductsByIds(
            @RequestBody List<Integer> productIds) {
        return ResponseEntity.ok(productService.getProductsByIds(productIds).stream().map(
                BasicProductDto::new).toList());
    }
}
