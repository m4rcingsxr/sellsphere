package com.sellsphere.client.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


// filter=name,'value, description with commas'
// filter=name1,value1&filter=name2,value2
@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;


    @GetMapping("/products")
    public ResponseEntity<ProductPageResponse> pageFilteredProducts(
            @ProductFilter ProductPageRequest pageRequest
    ) {
        ProductPageResponse page = productService.listProductsPage(pageRequest);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Map<String, Long>>> listFilterCounts(
            @ProductFilter ProductPageRequest pageRequest
    ) {
        Map<String, Map<String, Long>> filterCountMap = productService.getFilterCounts(pageRequest);

        return ResponseEntity.ok(filterCountMap);
    }

}
