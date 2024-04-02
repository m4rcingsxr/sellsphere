package com.sellsphere.admin.brand;

import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Constants;
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
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/brands.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BrandRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BrandRepository brandRepository;


    @Test
    void whenFindByName_thenReturnBrand() {

        // Given
        String expectedName = "Canon";

        // When
        Optional<Brand> brand = brandRepository.findByName(expectedName);

        // Then
        assertTrue(brand.isPresent(), "Brand should be present");
        assertEquals(expectedName, brand.get().getName(), "Brand name should match");
    }

    @ParameterizedTest(name = "Keyword: {0}, Expected Total: {1}")
    @CsvSource({"sandisk, 6, 5, 2, name",
                "s, 7, 5, 2, name",
                "SANDISK, 6, 5, 2, id",
                "canon, 1, 1, 1, name"})
    void whenSearchByKeyword_thenReturnMatchingBrands(String keyword, int expectedTotalElements,
                                                      int expectedContentSize, int expectedPages,
                                                      String sortField) {
        PageRequest pageRequest = PagingTestHelper.createPageRequest(0, 5, sortField,
                                                                     Constants.SORT_ASCENDING
        );

        // when
        Page<Brand> result = brandRepository.findAll(keyword, pageRequest);

        // then
        PagingTestHelper.assertPagingResults(result, expectedContentSize, expectedPages,
                                             expectedTotalElements, sortField, true
        );
    }

    @Test
    void whenSaveBrand_thenBrandIsSaved() {
        Brand newBrand = new Brand();
        newBrand.setName("NewBrand");
        newBrand.setLogo("newLogo.jpg");

        // When
        Brand savedBrand = brandRepository.save(newBrand);

        // Then
        assertNotNull(savedBrand.getId(), "Saved brand should have a non-null ID");
    }

    @Test
    void whenPersistBrandWithCategory_thenBrandIsPersistedAndJoinTableUpdated() {
        Category electronics = entityManager.find(Category.class, 1);

        // Given
        Brand brand = new Brand();
        brand.setName("TechGadgets");
        brand.setLogo("tech-logo.png");
        brand.addCategory(electronics);

        // When
        Brand savedBrand = brandRepository.saveAndFlush(brand);

        // Then
        assertNotNull(savedBrand.getId(), "Saved brand should have a non-null ID");
        assertEquals(1, savedBrand.getCategories().size(),
                     "Brand should have one category assigned"
        );

        Category category = savedBrand.getCategories().iterator().next();
        assertEquals("Electronics", category.getName(), "Category name should match");
        assertTrue(category.getBrands().stream().map(Brand::getName).anyMatch("TechGadgets"::equals), "Category name should match");
    }

    @Test
    void whenDeleteBrandById_thenBrandIsDeleted() {

        // Given
        Integer brandId = 1;

        // When
        brandRepository.deleteById(brandId);
        entityManager.flush();

        // Then
        Optional<Brand> deletedBrand = brandRepository.findById(brandId);
        assertTrue(deletedBrand.isEmpty(), "Brand should not be found after deletion");
    }

    @Test
    void whenCount_thenReturnCorrectNumber() {

        // Given
        long expectedCount = 10;

        // When
        long count = brandRepository.count();

        // Then
        assertEquals(expectedCount, count, "Brand count should match expected value");
    }

}