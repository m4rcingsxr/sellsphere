package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

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


}