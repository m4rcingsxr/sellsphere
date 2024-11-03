package com.sellsphere.admin.brand;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.PagingTestHelper;
import util.S3TestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(S3MockExtension.class)
@Sql(scripts = {"classpath:sql/categories.sql", "classpath:sql/brands.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/clean_categories.sql", "classpath:sql/clean_brands.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class BrandServiceIntegrationTest {

    private static final String BUCKET_NAME = "brand-bucket";
    private static S3Client s3Client;

    @Autowired
    private BrandService brandService;

    @BeforeAll
    static void setUpS3(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void givenValidBrandId_whenGetBrand_thenReturnBrand() throws BrandNotFoundException {
        // Given
        Integer brandId = 1;

        // When
        Brand brand = brandService.getBrandById(brandId);

        // Then
        assertNotNull(brand);
        assertEquals("Canon", brand.getName(), "Brand name should be Canon");
    }

    @Test
    void givenNonExistingBrandId_whenGetBrand_thenThrowBrandNotFoundException() {
        // Given
        Integer nonExistingBrandId = 999;

        // When/Then
        assertThrows(BrandNotFoundException.class, () -> brandService.getBrandById(nonExistingBrandId));
    }

    @Test
    void whenListAllBrands_thenReturnAllBrandsSortedByField() {
        // When
        List<Brand> brands = brandService.listAllBrands("name", Sort.Direction.ASC);

        // Then
        assertFalse(brands.isEmpty());
        assertEquals("Canon", brands.get(0).getName());
        assertEquals("Fujifilm", brands.get(1).getName());
    }

    @Test
    void givenPageNumber_whenListBrandsByPage_thenReturnBrandsForSpecificPage() {
        // Given
        int pageNum = 0;
        String sortField = "name";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(), "brandList", sortField, Sort.Direction.ASC, null);

        // When
        brandService.listBrandsByPage(pageNum, helper);

        // Then
        PagingTestHelper.assertPagingResults(helper, 1, 10, 10, sortField, true);
    }

    @Test
    void givenNewBrandWithFile_whenSaveBrand_thenSaveFileInS3AndReturnBrand() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "brand-logo.jpg", "image/jpeg", "Sample logo image".getBytes());

        Brand newBrand = new Brand();
        newBrand.setName("TestBrand");

        // When
        Brand savedBrand = brandService.saveBrand(newBrand, file);

        // Then
        assertNotNull(savedBrand.getId());
        assertEquals("brand-logo.jpg", savedBrand.getLogo());

        // Verify the file is saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "brand-photos/" + savedBrand.getId() + "/brand-logo.jpg", file.getInputStream());
    }

    @Test
    void givenBrandName_whenCheckBrandNameUnique_thenReturnCorrectResult() {
        // Given
        String existingName = "Canon";
        String uniqueName = "UniqueBrand";

        // When
        boolean isExistingNameUnique = brandService.isBrandNameUnique(null, existingName);
        boolean isUniqueNameUnique = brandService.isBrandNameUnique(null, uniqueName);

        // Then
        assertFalse(isExistingNameUnique);
        assertTrue(isUniqueNameUnique);
    }

    @Test
    void givenValidBrandId_whenDeleteBrand_thenBrandIsDeleted() throws BrandNotFoundException {
        // Given
        Integer brandId = 1;

        // When
        brandService.deleteBrandById(brandId);

        // Then
        assertThrows(BrandNotFoundException.class, () -> brandService.getBrandById(brandId));
    }

    @Test
    void givenNonExistingBrandId_whenDeleteBrand_thenThrowBrandNotFoundException() {
        // Given
        Integer nonExistingBrandId = 999;

        // When/Then
        assertThrows(BrandNotFoundException.class, () -> brandService.deleteBrandById(nonExistingBrandId));
    }

    @Test
    void givenBrandWithCategories_whenSaveBrand_thenCategoriesArePersisted() throws IOException {
        // Given
        Category electronicsCategory = new Category();
        electronicsCategory.setName("Electronics");

        Brand newBrand = new Brand();
        newBrand.setName("TechBrand");
        newBrand.addCategory(electronicsCategory);
        newBrand.setLogo("logo.jpg");

        // When
        Brand savedBrand = brandService.saveBrand(newBrand, null);

        // Then
        assertNotNull(savedBrand.getId());
        assertFalse(savedBrand.getCategories().isEmpty());
        assertEquals("Electronics", savedBrand.getCategories().iterator().next().getName());
    }
}
