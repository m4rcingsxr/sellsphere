package com.sellsphere.admin.brand;

import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql", "classpath:sql/categories.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/clean_categories.sql", "classpath:sql/clean_brands.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class BrandRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void givenValidBrandName_whenFindingByName_thenReturnBrand() {
        // Given
        String expectedName = "Canon";

        // When
        Optional<Brand> brand = brandRepository.findByName(expectedName);

        // Then
        assertTrue(brand.isPresent(), "Brand should be present");
        assertEquals(expectedName, brand.get().getName(), "Brand name should match");
    }

    @ParameterizedTest(name = "Keyword: {0}, Expected Total: {1}")
    @CsvSource({
            "sandisk, 6, 5, 2, name",
            "s, 7, 5, 2, name",
            "SANDISK, 6, 5, 2, id",
            "canon, 1, 1, 1, name"
    })
    void givenKeyword_whenSearchingByKeyword_thenReturnMatchingBrands(String keyword,
                                                                      int expectedTotalElements,
                                                                      int expectedContentSize,
                                                                      int expectedPages,
                                                                      String sortField) {
        // Given
        PageRequest pageRequest = PagingTestHelper.createPageRequest(0, 5, sortField, Sort.Direction.ASC);

        // When
        Page<Brand> result = brandRepository.findAll(keyword, pageRequest);

        // Then
        PagingTestHelper.assertPagingResults(result, expectedContentSize, expectedPages, expectedTotalElements, sortField, true);
    }

    @Test
    void givenNewBrand_whenSavingBrand_thenBrandIsSaved() {
        // Given
        Brand newBrand = new Brand();
        newBrand.setName("NewBrand");
        newBrand.setLogo("newLogo.png");

        // When
        Brand savedBrand = brandRepository.save(newBrand);

        // Then
        assertNotNull(savedBrand.getId(), "Saved brand should have a non-null ID");
    }

    @Test
    void givenExistingBrandWithCategories_whenSaving_thenCategoriesPersisted() {
        // Given
        Category electronicsCategory = entityManager.find(Category.class, 1);

        Brand newBrand = new Brand();
        newBrand.setName("TechBrand");
        newBrand.setLogo("tech.png");
        newBrand.addCategory(electronicsCategory);

        // When
        Brand savedBrand = brandRepository.save(newBrand);

        // Then
        assertNotNull(savedBrand.getId(), "Brand ID should not be null");
        assertEquals("TechBrand", savedBrand.getName(), "Brand name should match");
        assertEquals(1, savedBrand.getCategories().size(), "Brand should have one category associated");

        Category savedCategory = savedBrand.getCategories().iterator().next();
        assertEquals("Electronics", savedCategory.getName(), "Associated category name should match");
    }

    @Test
    void givenValidBrandId_whenDeletingBrand_thenBrandIsDeleted() {
        // Given
        Integer brandId = 1;

        // When
        brandRepository.deleteById(brandId);

        // Then
        Optional<Brand> deletedBrand = brandRepository.findById(brandId);
        assertTrue(deletedBrand.isEmpty(), "Brand should be deleted");
    }

    @Test
    void whenCountingBrands_thenReturnCorrectCount() {
        // Given
        long expectedCount = 10;

        // When
        long brandCount = brandRepository.count();

        // Then
        assertEquals(expectedCount, brandCount, "Brand count should match the expected value");
    }
}
