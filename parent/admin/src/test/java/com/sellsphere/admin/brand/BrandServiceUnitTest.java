package com.sellsphere.admin.brand;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BrandServiceUnitTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private MultipartFile file;

    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @InjectMocks
    private BrandService brandService;

    @Test
    void givenValidBrandId_whenGetBrand_thenReturnBrand() throws BrandNotFoundException {
        // Given
        Integer brandId = 1;
        Brand brand = new Brand();
        brand.setId(brandId);

        given(brandRepository.findById(brandId)).willReturn(Optional.of(brand));

        // When
        Brand foundBrand = brandService.getBrandById(brandId);

        // Then
        assertNotNull(foundBrand);
        assertEquals(brandId, foundBrand.getId());
    }

    @Test
    void givenNonExistingBrandId_whenGetBrand_thenThrowBrandNotFoundException() {
        // Given
        Integer brandId = 999;

        given(brandRepository.findById(brandId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(BrandNotFoundException.class, () -> brandService.getBrandById(brandId));
    }

    @Test
    void givenNewBrandWithFile_whenSaveBrand_thenFileShouldBeSaved() throws IOException {
        // Given
        Brand newBrand = new Brand();
        newBrand.setName("TestBrand");

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("logo.png");

        given(brandRepository.save(any(Brand.class))).willAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            Brand savedBrand = brandService.saveBrand(newBrand, file);

            // Then
            then(brandRepository).should().save(any(Brand.class));
            mockedFileService.verify(() -> FileService.saveSingleFile(eq(file), anyString(), eq("logo.png")));
        }
    }

    @Test
    void givenExistingBrandWithoutFile_whenSaveBrand_thenBrandShouldBeSavedWithoutFileHandling() throws IOException {
        // Given
        Brand existingBrand = new Brand();
        existingBrand.setId(1);

        given(brandRepository.save(any(Brand.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        Brand savedBrand = brandService.saveBrand(existingBrand, null);

        // Then
        then(brandRepository).should().save(any(Brand.class));
        assertNotNull(savedBrand.getId());
    }

    @Test
    void givenValidPageNumber_whenListBrandsByPage_thenHelperMethodCalled() {
        // Given
        Integer pageNum = 1;

        // When
        brandService.listBrandsByPage(pageNum, pagingAndSortingHelper);

        // Then
        then(pagingAndSortingHelper).should().listEntities(eq(pageNum), anyInt(), eq(brandRepository));
    }

    @Test
    void whenListAllBrands_thenReturnSortedBrands() {
        // Given
        Brand brand1 = new Brand();
        brand1.setName("Canon");

        Brand brand2 = new Brand();
        brand2.setName("Fujifilm");

        given(brandRepository.findAll(any(Sort.class))).willReturn(List.of(brand1, brand2));

        // When
        List<Brand> brands = brandService.listAllBrands("name", Sort.Direction.ASC);

        // Then
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Canon", brands.get(0).getName());
        assertEquals("Fujifilm", brands.get(1).getName());
    }

    @Test
    void givenUniqueBrandName_whenCheckBrandNameUnique_thenReturnTrue() {
        // Given
        String uniqueBrandName = "UniqueBrand";

        given(brandRepository.findByName(uniqueBrandName)).willReturn(Optional.empty());

        // When
        boolean isUnique = brandService.isBrandNameUnique(null, uniqueBrandName);

        // Then
        assertTrue(isUnique);
    }

    @Test
    void givenExistingBrandName_whenCheckBrandNameUnique_thenReturnFalse() {
        // Given
        String existingBrandName = "Canon";
        Brand brand = new Brand();
        brand.setId(1);

        given(brandRepository.findByName(existingBrandName)).willReturn(Optional.of(brand));

        // When
        boolean isUnique = brandService.isBrandNameUnique(null, existingBrandName);

        // Then
        assertFalse(isUnique);
    }

    @Test
    void givenValidBrandId_whenDeleteBrand_thenBrandShouldBeDeleted() throws BrandNotFoundException {
        // Given
        Integer brandId = 1;
        Brand brand = new Brand();
        brand.setId(brandId);

        given(brandRepository.findById(brandId)).willReturn(Optional.of(brand));

        // When
        brandService.deleteBrandById(brandId);

        // Then
        then(brandRepository).should().delete(brand);
    }

    @Test
    void givenNonExistingBrandId_whenDeleteBrand_thenThrowBrandNotFoundException() {
        // Given
        Integer brandId = 999;

        given(brandRepository.findById(brandId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(BrandNotFoundException.class, () -> brandService.deleteBrandById(brandId));
    }
}
