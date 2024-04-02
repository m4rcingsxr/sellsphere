package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Constants;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import util.PagingTestHelper;

@Transactional
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BrandServiceIntegrationTest {

    @Autowired
    private BrandService brandService;


    @Test
    void givenKeyword_whenPageCategories_thenReturnCorrectContent() {
        // Given
        int pageNum = 0;
        int expectedContentSize = 6;
        int expectedTotalElements = 6;
        int expectedPages = 1;
        String keyword = "sandisk";
        String sortField = "name";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "categoryList", sortField,
                                                                   Constants.SORT_ASCENDING, keyword
        );

        // When
        brandService.listPage(pageNum, helper);

        // Then
        PagingTestHelper.assertPagingResults(helper, expectedPages, expectedContentSize,
                                             expectedTotalElements, sortField, true
        );

    }

}