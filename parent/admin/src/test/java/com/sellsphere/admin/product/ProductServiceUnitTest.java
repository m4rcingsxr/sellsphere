package com.sellsphere.admin.product;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenExistingProductId_whenGetProduct_thenReturnProduct() throws ProductNotFoundException {
        // Given
        Integer existingProductId = 1;
        Product mockProduct = new Product();
        when(productRepository.findById(existingProductId)).thenReturn(Optional.of(mockProduct));

        // When
        Product result = productService.get(existingProductId);

        // Then
        assertNotNull(result);
        assertEquals(mockProduct, result);
    }

    @Test
    public void givenNonExistingProductId_whenGetProduct_thenThrowProductNotFoundException() {
        // Given
        Integer nonExistingProductId = 999;
        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productService.get(nonExistingProductId);
        });
    }

}