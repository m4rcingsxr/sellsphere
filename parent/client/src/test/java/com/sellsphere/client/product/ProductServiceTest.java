package com.sellsphere.client.product;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenMultipleProducts_whenGetFilterCounts_thenReturnCorrectCounts() {
        // Given
        ProductPageRequest pageRequest = new ProductPageRequest();

        Product product1 = new Product();
        product1.setDetails(Arrays.asList(new ProductDetail("Color", "Red", product1),
                                          new ProductDetail("Size", "M", product1)
        ));

        Product product2 = new Product();
        product2.setDetails(Arrays.asList(new ProductDetail("Color", "Red", product2),
                                          new ProductDetail("Size", "L", product2)
        ));

        Product product3 = new Product();
        product3.setDetails(Arrays.asList(new ProductDetail("Color", "Blue", product3),
                                          new ProductDetail("Size", "M", product3)
        ));

        List<Product> products = Arrays.asList(product1, product2, product3);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        // When
        Map<String, Map<String, Long>> filterCounts = productService.getFilterCounts(pageRequest);

        // Then
        assertThat(filterCounts).hasSize(2);
        assertThat(filterCounts.get("Color")).containsEntry("Red", 2L);
        assertThat(filterCounts.get("Color")).containsEntry("Blue", 1L);
        assertThat(filterCounts.get("Size")).containsEntry("M", 2L);
        assertThat(filterCounts.get("Size")).containsEntry("L", 1L);
    }
}
