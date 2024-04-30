package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    public ProductPageResponse listProductsPage(ProductPageRequest pageRequest) {
        List<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest));

        return ProductPageResponse.builder().content(products).build();
    }





}
