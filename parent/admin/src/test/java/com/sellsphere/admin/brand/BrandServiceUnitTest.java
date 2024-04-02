package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BrandServiceUnitTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private BrandRepository brandRepository;
    
    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @InjectMocks
    private BrandService brandService;
    
    @Test
    void listPage_WhenCalled_VerifyHelperIsUsedWithCorrectParameters() {
        // Given
        int pageNum = 1;

        // When
        brandService.listPage(pageNum, pagingAndSortingHelper);

        // Then
        then(pagingAndSortingHelper).should().listEntities(eq(pageNum), eq(PAGE_SIZE), any(BrandRepository.class));
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

}