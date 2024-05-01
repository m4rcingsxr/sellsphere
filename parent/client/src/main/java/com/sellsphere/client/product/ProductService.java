package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;


    public ProductPageResponse listProductsPage(ProductPageRequest pageRequest) {
        PageRequest pageable = PageRequest.of(pageRequest.getPageNum(), PAGE_SIZE);
        Page<Product> products = productRepository.findAll(
                ProductSpecification.filterProducts(pageRequest), pageable);

        return ProductPageResponse.builder()
                .content(products.map(BasicProductDto::new).toList())
                .page(pageRequest.getPageNum())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .pageSize(PAGE_SIZE)
                .build();
    }

    public Map<String, Map<String, Long>> getFilterCounts(ProductPageRequest pageRequest) {
        List<Product> filteredProducts = productRepository
                .findAll(ProductSpecification.filterProducts(pageRequest));

        return filteredProducts.stream()
                .flatMap(product -> product.getDetails().stream())
                .collect(Collectors.groupingBy(
                        ProductDetail::getName,
                        Collectors.groupingBy(
                                ProductDetail::getValue,
                                Collectors.counting()
                        )
                ));
    }


}
