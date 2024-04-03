package com.sellsphere.admin.brand;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.FileService;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Constants;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.PagingTestHelper;
import util.S3TestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ExtendWith(S3MockExtension.class)
class BrandServiceIntegrationTest {

    private static final String BUCKET_NAME = "my-demo-test-bucket";
    private static S3Client s3Client;

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeAll
    static void setUpClient(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void givenValidBrandAndFile_whenSaving_thenShouldSaveFileAndBrand() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Sample image content".getBytes()
        );
        Brand brand = new Brand();
        brand.setName("TestBrand");

        // When
        Brand savedBrand = brandService.save(brand, file);

        // Then
        assertNotNull(savedBrand.getId());
        assertEquals("test-image.jpg", savedBrand.getLogo());

        // Verify file is saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "brand-photos/" + savedBrand.getId() + "/test-image.jpg", file.getInputStream());

        // Verify brand is saved in the repository
        Brand fetchedBrand = brandRepository.findById(savedBrand.getId()).orElse(null);
        assertNotNull(fetchedBrand);
        assertEquals("test-image.jpg", fetchedBrand.getLogo());
    }

    @Test
    void givenValidBrandWithoutFile_whenUpdate_thenShouldSaveBrandWithExistingLogo() throws IOException {
        // Given
        Brand brand = new Brand();
        brand.setName("TestBrand");
        brand.setLogo("logo.jpg");

        // When
        Brand savedBrand = brandService.save(brand, null);

        // Then
        assertNotNull(savedBrand.getId());
        assertNotNull(savedBrand.getLogo());

        Brand fetchedBrand = brandRepository.findById(savedBrand.getId()).orElse(null);
        assertNotNull(fetchedBrand);
        assertNotNull(fetchedBrand.getLogo());
    }

    @Test
    void givenKeyword_whenPageBrands_thenReturnCorrectContent() {

        // Given
        int pageNum = 0;
        int expectedContentSize = 6;
        int expectedTotalElements = 6;
        int expectedPages = 1;
        String keyword = "sandisk";
        String sortField = "name";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "brandList", sortField,
                                                                   Constants.SORT_ASCENDING, keyword
        );

        // When
        brandService.listPage(pageNum, helper);

        // Then
        PagingTestHelper.assertPagingResults(helper, expectedPages, expectedContentSize,
                                             expectedTotalElements, sortField, true
        );
    }

    @Test
    void whenValidId_thenReturnBrand() throws BrandNotFoundException {

        // Given
        Integer brandId = 1;

        // When
        Brand brand = brandService.get(brandId);

        // Then
        assertNotNull(brand);
        assertEquals(brandId, brand.getId());
    }

    @Test
    void whenInvalidId_thenThrowException() {

        // Given
        Integer brandId = 999;

        // When & Then
        assertThrows(BrandNotFoundException.class, () -> brandService.get(brandId));
    }
}
