package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;


    public ProductPageResponse listProductsPage(ProductPageRequest pageRequest) {
        PageRequest pageable = PageRequest.of(pageRequest.getPageNum(), PAGE_SIZE);
        Page<Product> products = productRepository.findAll(ProductSpecification.filterProducts(pageRequest), pageable);

        return ProductPageResponse.builder()
                .content(products.map(BasicProductDto::new).toList())
                .page(pageRequest.getPageNum())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .pageSize(PAGE_SIZE)
                .build();
    }



}
