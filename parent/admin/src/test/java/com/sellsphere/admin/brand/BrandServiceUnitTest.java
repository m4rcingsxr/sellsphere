package com.sellsphere.admin.brand;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceUnitTest {

    private static final int PAGE_SIZE = 10;
    private static final String BRAND_PHOTOS_DIR = "brand-photos/";

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private BrandService brandService;

    @Test
    void listPage_WhenCalled_VerifyHelperIsUsedWithCorrectParameters() {
        // Given
        int pageNum = 1;

        // When
        brandService.listPage(pageNum, pagingAndSortingHelper);

        // Then
        then(pagingAndSortingHelper).should().listEntities(eq(pageNum), eq(PAGE_SIZE),
                                                           any(BrandRepository.class)
        );
    }

    @Test
    void whenGetById_ExistingBrand_ShouldReturnBrand() throws BrandNotFoundException {
        // Given
        Integer id = 1;
        Brand expectedBrand = new Brand();
        expectedBrand.setId(id);
        given(brandRepository.findById(id)).willReturn(Optional.of(expectedBrand));

        // When
        Brand actualBrand = brandService.get(id);

        // Then
        assertEquals(expectedBrand, actualBrand);
        then(brandRepository).should(times(1)).findById(id);
    }

    @Test
    void whenGetById_NonExistingBrand_ShouldThrowException() {
        // Given
        Integer nonExistingId = 999;
        given(brandRepository.findById(nonExistingId)).willReturn(Optional.empty());

        // When, Then
        assertThrows(BrandNotFoundException.class, () -> brandService.get(nonExistingId));
        then(brandRepository).should(times(1)).findById(nonExistingId);
    }

    @Test
    void givenValidBrandAndFile_whenSave_thenBrandIsSavedWithLogo() throws IOException {
        // given
        Brand brand = new Brand();
        MultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1, 2, 3, 4});

        Brand savedBrand = new Brand();
        savedBrand.setId(1);
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> {
            Brand arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });

        // when
        Brand result = brandService.save(brand, file);

        // then
        assertEquals("logo.png", result.getLogo());
        verify(fileService).saveSingleFile(file, BRAND_PHOTOS_DIR + savedBrand.getId(), "logo.png");
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void givenValidBrandWithoutFile_whenSave_thenBrandIsSavedWithoutLogo() throws IOException {
        // given
        Brand brand = new Brand();
        MultipartFile file = null;

        Brand savedBrand = new Brand();
        savedBrand.setId(1);
        when(brandRepository.save(brand)).thenReturn(savedBrand);

        // when
        Brand result = brandService.save(brand, file);

        // then
        assertNull(result.getLogo());
        verify(fileService, never()).saveSingleFile(any(), anyString(), anyString());
        verify(brandRepository).save(brand);
    }

    @Test
    void givenValidBrandAndEmptyFile_whenSave_thenBrandIsSavedWithoutLogo() throws IOException {
        // given
        Brand brand = new Brand();
        MultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[]{});

        Brand savedBrand = new Brand();
        savedBrand.setId(1);
        when(brandRepository.save(brand)).thenReturn(savedBrand);

        // when
        Brand result = brandService.save(brand, file);

        // then
        assertNull(result.getLogo());
        verify(fileService, never()).saveSingleFile(any(), anyString(), anyString());
        verify(brandRepository).save(brand);
    }

    @Test
    void givenBrandWithCategories_whenRemoveLeadingDashFromCategories_thenDashesAreRemoved()
            throws IOException {
        // given
        Brand brand = new Brand();
        Category category = new Category();
        category.setName("Category");
        Category category1 = new Category();
        category1.setName("-Category1");
        Category category2 = new Category();
        category2.setName("--Category2");

        category.addChild(category1);
        category1.addChild(category2);

        brand.addCategory(category);

        // when
        brandService.save(brand, null);

        // then
        assertEquals("Category", category.getName());
        assertEquals("Category1", category1.getName());
        assertEquals("Category2", category2.getName());
    }

    @Test
    void isNameUnique_WithNonExistingName_ShouldReturnTrue() {
        // Given
        String nonExistingName = "NonExistingBrand";
        given(brandRepository.findByName(nonExistingName)).willReturn(Optional.empty());

        // When
        boolean result = brandService.isNameUnique(null, nonExistingName);

        // Then
        assertTrue(result);
    }

    @Test
    void isNameUnique_WithExistingNameAndSameId_ShouldReturnTrue() {
        // Given
        String existingName = "ExistingBrand";
        Integer existingId = 1;
        Brand existingBrand = new Brand();
        existingBrand.setId(existingId);
        existingBrand.setName(existingName);
        given(brandRepository.findByName(existingName)).willReturn(Optional.of(existingBrand));

        // When
        boolean result = brandService.isNameUnique(existingId, existingName);

        // Then
        assertTrue(result);
    }

    @Test
    void isNameUnique_WithExistingNameAndDifferentId_ShouldReturnFalse() {
        // Given
        String existingName = "ExistingBrand";
        Integer differentId = 2;
        Brand existingBrand = new Brand();
        existingBrand.setId(1);
        existingBrand.setName(existingName);
        given(brandRepository.findByName(existingName)).willReturn(Optional.of(existingBrand));

        // When
        boolean result = brandService.isNameUnique(differentId, existingName);

        // Then
        assertFalse(result);
    }

    @Test
    void whenDeleteExistingBrand_ShouldCallRepositoryDelete() throws BrandNotFoundException {
        // Given
        Integer brandId = 1;
        Brand existingBrand = new Brand();
        existingBrand.setId(brandId);
        given(brandRepository.findById(brandId)).willReturn(Optional.of(existingBrand));

        // When
        brandService.delete(brandId);

        // Then
        then(brandRepository).should().delete(existingBrand);
    }

    @Test
    void whenDeleteNotExistingBrand_ShouldThrowBrandNotFoundException() {
        // Given
        Integer brandId = -1;
        given(brandRepository.findById(brandId)).willReturn(Optional.empty());

        // When
        // Then
        assertThrows(BrandNotFoundException.class, () -> brandService.delete(brandId));
    }

}