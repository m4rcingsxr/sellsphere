package com.sellsphere.admin.product;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductImage;
import com.sellsphere.common.entity.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @Mock
    private MultipartFile primaryImage;

    @Mock
    private MultipartFile extraImage1;

    @Mock
    private MultipartFile extraImage2;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenValidProduct_whenSaveProduct_thenVerifyRepositorySaveAndFileServiceCalled() throws IOException {
        // Given
        Product product = new Product();
        product.setName("New Product");

        // Create an array of mocked MultipartFile
        MultipartFile[] extraImages = new MultipartFile[]{extraImage1, extraImage2};

        given(productRepository.save(any(Product.class))).willReturn(product);

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class);
             MockedStatic<ProductHelper> mockedProductHelper = mockStatic(ProductHelper.class)) {

            // When
            productService.saveProduct(product, primaryImage, extraImages);

            // Then
            mockedProductHelper.verify(() -> ProductHelper.addProductImages(eq(product), eq(extraImages)));
            mockedProductHelper.verify(() -> ProductHelper.saveExtraImages(eq(product), eq(extraImages)));
            mockedProductHelper.verify(() -> ProductHelper.savePrimaryImage(eq(product), eq(primaryImage)));

            // Verify repository interactions
            then(productRepository).should().save(any(Product.class));
        }
    }

    @Test
    void givenValidProductId_whenGetProductById_thenVerifyRepositoryCalled() throws ProductNotFoundException {
        // Given
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // When
        productService.getProductById(productId);

        // Then
        then(productRepository).should().findById(productId);
    }

    @Test
    void givenNonExistingProductId_whenGetProductById_thenThrowProductNotFoundExceptionAndVerifyRepositoryCalled() {
        // Given
        Integer productId = 999;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
        then(productRepository).should().findById(productId);
    }

    @Test
    void givenValidSortOptions_whenListAllProducts_thenVerifyRepositoryCalled() {
        // Given
        Sort.Direction sortDir = Sort.Direction.ASC;

        // When
        productService.listAllProducts("name", sortDir);

        // Then
        then(productRepository).should().findAll(any(Sort.class));
    }

    @Test
    void givenNewProductName_whenIsProductNameUnique_thenVerifyRepositoryCalled() {
        // Given
        given(productRepository.findByName(anyString())).willReturn(Optional.empty());

        // When
        boolean isUnique = productService.isProductNameUnique(null, "New Product");

        // Then
        assertTrue(isUnique);
        then(productRepository).should().findByName(anyString());
    }


    @Test
    void givenValidProductId_whenDeleteProductById_thenVerifyRepositoryAndS3UtilityCalls() throws ProductNotFoundException {
        // Given
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        try (MockedStatic<S3Utility> mockedS3Utility = mockStatic(S3Utility.class)) {
            // When
            productService.deleteProductById(productId);

            // Then
            then(productRepository).should().findById(productId);
            then(productRepository).should().delete(any(Product.class));
            mockedS3Utility.verify(() -> S3Utility.removeFolder("product-photos/" + productId));
        }
    }

    @Test
    void givenNonExistingProductId_whenDeleteProductById_thenThrowProductNotFoundExceptionAndVerifyRepositoryCalled() {
        // Given
        Integer productId = 999;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProductById(productId));
        then(productRepository).should().findById(productId);
    }

    @Test
    void givenValidProductId_whenUpdateProductEnabledStatus_thenVerifyRepositorySaveCalled() throws ProductNotFoundException {
        // Given
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // When
        productService.updateProductEnabledStatus(productId, true);

        // Then
        then(productRepository).should().findById(productId);
        then(productRepository).should().save(product);
    }


    /**
     * Test to verify that the S3Utility is called correctly when a product is deleted.
     */
    @Test
    void givenValidProductId_whenDeleteProductById_thenVerifyS3UtilityCalled() throws ProductNotFoundException {
        // Given
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // Mock static methods from S3Utility
        try (MockedStatic<S3Utility> mockedS3Utility = mockStatic(S3Utility.class)) {
            // When
            productService.deleteProductById(productId);

            // Then
            // Verify S3Utility interaction for removing the product folder
            mockedS3Utility.verify(() -> S3Utility.removeFolder("product-photos/" + productId));
        }
    }

    @Test
    void givenProductAndImages_whenSaveProduct_thenProductImagesAreAddedAndFileServiceIsMocked() throws IOException {
        // Given
        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");

        MultipartFile[] extraImages = {extraImage1, extraImage2};

        given(extraImage1.getOriginalFilename()).willReturn("extra1.jpg"); // Mock filenames
        given(extraImage2.getOriginalFilename()).willReturn("extra2.jpg");
        given(primaryImage.getOriginalFilename()).willReturn("primary.jpg");

        given(productRepository.save(any(Product.class))).willReturn(product);

        // Mock static methods from FileService
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            productService.saveProduct(product, primaryImage, extraImages
            );

            // Then
            // Assert that images were added to the product
            assertEquals(2, product.getImages().size(), "Extra images should be added to the product.");
            assertTrue(product.getImages().stream().anyMatch(img -> img.getName().equals("extra1.jpg")),
                       "First extra image should be added.");
            assertTrue(product.getImages().stream().anyMatch(img -> img.getName().equals("extra2.jpg")),
                       "Second extra image should be added.");

            // Verify primary image is set
            assertEquals("primary.jpg", product.getMainImage(), "Primary image should be set.");

            // Verify FileService interaction for saving files
            mockedFileService.verify(() -> FileService.saveSingleFile(eq(primaryImage), anyString(), anyString()));
            mockedFileService.verify(() -> FileService.saveFile(eq(extraImage1), anyString(), anyString()));
            mockedFileService.verify(() -> FileService.saveFile(eq(extraImage2), anyString(), anyString()));
        }
    }

    @Test
    void givenProductWithoutImages_whenSaveProduct_thenNoFileServiceInteraction() throws IOException {
        // Given
        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");

        given(productRepository.save(any(Product.class))).willReturn(product);

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            productService.saveProduct(product, null, null);

            // Then
            // Verify no images were added
            assertTrue(product.getImages().isEmpty(), "No extra images should be added.");
            assertNull(product.getMainImage(), "No primary image should be set.");

            // Verify that FileService methods were never called
            mockedFileService.verifyNoInteractions();
        }
    }

    @Test
    void givenProductWithExistingImages_whenSaveProduct_thenNewImagesAreAdded() throws IOException {
        // Given
        Product product = new Product();
        product.setId(1);
        product.setName("Existing Product");

        ProductImage existingImage = new ProductImage();
        existingImage.setName("existing-image.jpg");
        product.addProductImage(existingImage);

        MultipartFile[] newExtraImages = {extraImage1};

        given(extraImage1.getOriginalFilename()).willReturn("extra1.jpg");
        given(primaryImage.getOriginalFilename()).willReturn("primary.jpg");

        given(productRepository.save(any(Product.class))).willReturn(product);

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            productService.saveProduct(product, primaryImage, newExtraImages
            );

            // Then
            // Verify that the existing image remains and the new one is added
            assertEquals(2, product.getImages().size(), "There should be two images, including the existing one.");
            assertTrue(product.getImages().contains(existingImage), "Existing image should still be present.");
            assertTrue(product.getImages().stream().anyMatch(img -> img.getName().equals("extra1.jpg")),
                       "New extra image should be added.");

            // Verify FileService interaction for saving new images
            mockedFileService.verify(() -> FileService.saveFile(eq(extraImage1), anyString(), anyString()));
            mockedFileService.verify(() -> FileService.saveSingleFile(eq(primaryImage), anyString(), anyString()));
        }
    }


}
