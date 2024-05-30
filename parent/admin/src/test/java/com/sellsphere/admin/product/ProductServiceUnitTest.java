package com.sellsphere.admin.product;

import com.sellsphere.admin.S3Utility;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductNotFoundException;
import com.sellsphere.common.entity.SettingNotFoundException;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    void givenNonExistingProductId_whenGetProduct_thenThrowProductNotFoundException() {
        // Given
        Integer nonExistingProductId = 999;
        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productService.get(nonExistingProductId);
        });
    }

    @Test
    void givenNewProduct_whenSave_thenProductIsSavedAndImagesAreHandled()
            throws IOException, StripeException, SettingNotFoundException,
            CurrencyNotFoundException {
        // Given
        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");
        product.setAlias("test_product");
        product.setCreatedTime(LocalDateTime.now());

        when(productRepository.save(any(Product.class))).thenReturn(product);
        MockMultipartFile newPrimaryImage = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile extraImage = new MockMultipartFile("file", "extra.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MultipartFile[] extraImages = {extraImage};

        // Mock static methods
        try (MockedStatic<ProductHelper> mockedProductHelper = mockStatic(ProductHelper.class)) {
            mockedProductHelper.when(() -> ProductHelper.addProductImages(any(Product.class), any(MultipartFile[].class)))
                    .then(invocation -> null);
            mockedProductHelper.when(() -> ProductHelper.saveExtraImages(any(Product.class), any(MultipartFile[].class)))
                    .then(invocation -> null);
            mockedProductHelper.when(() -> ProductHelper.savePrimaryImage(any(Product.class), any(MultipartFile.class)))
                    .then(invocation -> null);

            // When
            Product savedProduct = productService.save(product, newPrimaryImage, extraImages);

            // Then
            assertNotNull(savedProduct);
            assertEquals("Test Product", savedProduct.getName());
            assertEquals("test_product", savedProduct.getAlias());

            verify(productRepository, times(1)).save(product);
            mockedProductHelper.verify(() -> ProductHelper.addProductImages(product, extraImages), times(1));
            mockedProductHelper.verify(() -> ProductHelper.saveExtraImages(product, extraImages), times(1));
            mockedProductHelper.verify(() -> ProductHelper.savePrimaryImage(product, newPrimaryImage), times(1));
        }
    }

    @Test
    void givenExistingProductWithSameId_whenIsNameUnique_thenReturnTrue() {
        // Given
        Integer existingProductId = 1;
        String existingProductName = "Existing Product";
        Product existingProduct = new Product();
        existingProduct.setId(existingProductId);
        existingProduct.setName(existingProductName);

        when(productRepository.findByName(existingProductName)).thenReturn(Optional.of(existingProduct));

        // When
        boolean result = productService.isNameUnique(existingProductId, existingProductName);

        // Then
        assertTrue(result);
    }

    @Test
    void givenExistingProductWithDifferentId_whenIsNameUnique_thenReturnFalse() {
        // Given
        Integer existingProductId = 1;
        Integer differentProductId = 2;
        String existingProductName = "Existing Product";
        Product existingProduct = new Product();
        existingProduct.setId(existingProductId);
        existingProduct.setName(existingProductName);

        when(productRepository.findByName(existingProductName)).thenReturn(Optional.of(existingProduct));

        // When
        boolean result = productService.isNameUnique(differentProductId, existingProductName);

        // Then
        assertFalse(result);
    }

    @Test
    void givenNonExistingProductName_whenIsNameUnique_thenReturnTrue() {
        // Given
        Integer anyProductId = 1;
        String nonExistingProductName = "Non-Existing Product";

        when(productRepository.findByName(nonExistingProductName)).thenReturn(Optional.empty());

        // When
        boolean result = productService.isNameUnique(anyProductId, nonExistingProductName);

        // Then
        assertTrue(result);
    }

    @Test
    void givenExistingProductId_whenDeleteProduct_thenShouldRemoveProductAndFiles()
            throws ProductNotFoundException, StripeException {

        // Given
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        try (MockedStatic<S3Utility> mockedS3Utility = mockStatic(S3Utility.class)) {
            mockedS3Utility.when(() -> S3Utility.removeFolder("product-photos/" + productId))
                    .then(invocation -> null);

            // When
            productService.deleteProduct(productId);

            // Then
            verify(productRepository, times(1)).delete(product);
            mockedS3Utility.verify(() -> S3Utility.removeFolder("product-photos/" + productId), times(1));
        }
    }

    @Test
    void givenNonExistingProductId_whenDeleteProduct_thenThrowProductNotFoundException() {
        // Given
        Integer nonExistingProductId = 999;

        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(nonExistingProductId));
    }

    @Test
    void givenExistingProductId_whenUpdateProductEnabledStatus_thenStatusIsUpdated()
            throws ProductNotFoundException, StripeException {
        // Given
        Integer existingProductId = 1;
        boolean newStatus = false;
        Product existingProduct = new Product();
        existingProduct.setId(existingProductId);
        existingProduct.setEnabled(true);

        when(productRepository.findById(existingProductId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        // When
        productService.updateProductEnabledStatus(existingProductId, newStatus);

        // Then
        assertEquals(newStatus, existingProduct.isEnabled());
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void givenNonExistingProductId_whenUpdateProductEnabledStatus_thenThrowProductNotFoundException() {
        // Given
        Integer nonExistingProductId = 999;
        boolean newStatus = false;

        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateProductEnabledStatus(nonExistingProductId, newStatus);
        });
    }



}