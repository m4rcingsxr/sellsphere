package com.sellsphere.client.product;

import com.sellsphere.common.entity.BasicProductDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/products.sql")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @ParameterizedTest
    @CsvSource({
            "'brand,Apple|Color,Red', 'Product One,Product Two', 2, 2, 1",
            "'brand,Apple|Size,Medium', 'Product One,Product Two', 2, 2, 1",
            "'brand,Apple|Weight,1.5kg', 'Product One', 1, 1, 1",
            "'brand,Apple|Material,Plastic', 'Product Two', 1, 1, 1",
            "'brand,Apple|Color,Green', 'Product Three', 1, 1, 1",
            "'brand,Apple|Size,Large', 'Product Three', 1, 1, 1",
            "'brand,Apple|Color,Black', '', 0, 0, 0",
            "'brand,Apple|Size,Small', '', 0, 0, 0",
            "'brand,Apple|Warranty,3 years', '', 0, 0, 0",
            "'brand,Apple|Warranty,5 years', '', 0, 0, 0",
            "'brand,Apple|Color,Red|Size,Medium', 'Product One,Product Two', 2, 2, 1",
            "'brand,Apple|Color,Green|Weight,2kg', 'Product Three', 1, 1, 1"
    })
    void givenFilters_whenFindAllWithProductSpecificationAndPagination_thenShouldReturnMatchingFilterExpectedProductsWithPagination(
            String filter, String expectedProducts, long totalElements, int contentSize, int totalPages) {
        // Prepare the filters
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null, 0);
        pageRequest.setCategoryId(1);

        // Get the filtered products with pagination
        ProductPageResponse response = productService.listProductsPage(pageRequest);

        // Convert expected products to a list
        List<String> expectedProductList = expectedProducts.isEmpty() ? List.of() : List.of(expectedProducts.split(","));

        // Assert the result
        List<String> resultNames = response.getContent().stream().map(BasicProductDto::getName).collect(Collectors.toList());
        assertThat(resultNames).containsExactlyInAnyOrderElementsOf(expectedProductList);

        // Assert the page information
        assertThat(response.getTotalElements()).isEqualTo(totalElements);
        assertThat(response.getContent().size()).isEqualTo(contentSize);
        assertThat(response.getTotalPages()).isEqualTo(totalPages);
    }

    @ParameterizedTest
    @CsvSource({
            "'brand,Apple|Color,Red', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1'",
            "'brand,Apple|Size,Medium', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1'",
            "'brand,Apple|Weight,1.5kg', 'Color=Red=1;Size=Medium=1;Weight=1.5kg=1'",
            "'brand,Apple|Material,Plastic', 'Color=Red=1;Material=Plastic=1;Size=Medium=1'",
            "'brand,Apple|Color,Green', 'Color=Green=1;Size=Large=1;Weight=2kg=1'",
            "'brand,Apple|Size,Large', 'Color=Green=1;Size=Large=1;Weight=2kg=1'",
            "'brand,Apple|Color,Black', ''",
            "'brand,Apple|Size,Small', ''",
            "'brand,Apple|Warranty,3 years', ''",
            "'brand,Apple|Warranty,5 years', ''",
            "'brand,Apple|Color,Red|Size,Medium', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1'",
            "'brand,Apple|Color,Green|Weight,2kg', 'Color=Green=1;Size=Large=1;Weight=2kg=1'"
    })
    void givenFilters_whenGetAvailableFilterCounts_thenShouldReturnMatchingCounts(String filter, String expectedCounts) {
        // Prepare the filters
        String[] filters = filter.split("\\|");
        ProductPageRequest pageRequest = new ProductPageRequest(filters, "laptops", null, 0);
        pageRequest.setCategoryId(1);

        // Get the filter counts
        Map<String, Map<String, Long>> filterCounts = productService.getAvailableFilterCounts(pageRequest);

        // Convert expected counts to a map
        Map<String, Map<String, Long>> expectedCountsMap = parseExpectedCounts(expectedCounts);

        // Assert the result
        assertThat(filterCounts).isEqualTo(expectedCountsMap);
    }

    private Map<String, Map<String, Long>> parseExpectedCounts(String expectedCounts) {
        if (expectedCounts.isEmpty()) {
            return Map.of();
        }
        return List.of(expectedCounts.split(";")).stream()
                .map(s -> s.split("="))
                .collect(Collectors.groupingBy(
                        arr -> arr[0],
                        Collectors.toMap(arr -> arr[1], arr -> Long.parseLong(arr[2]))
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "'brand,Apple|Color,Red', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1'",
            "'brand,Apple|Size,Medium', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1'",
            "'brand,Apple|Weight,1.5kg', 'Color=Red=1;Size=Medium=1;Weight=1.5kg=1', 'Color=Red=1;Size=Medium=1;Weight=1.5kg=1'",
            "'brand,Apple|Material,Plastic', 'Color=Red=1;Material=Plastic=1;Size=Medium=1', 'Color=Red=1;Material=Plastic=1;Size=Medium=1'",
            "'brand,Apple|Color,Green', 'Color=Green=1;Size=Large=1;Weight=2kg=1', 'Color=Green=1;Size=Large=1;Weight=2kg=1'",
            "'brand,Apple|Size,Large', 'Color=Green=1;Size=Large=1;Weight=2kg=1', 'Color=Green=1;Size=Large=1;Weight=2kg=1'",
            "'brand,Apple|Color,Black', '', ''",
            "'brand,Apple|Size,Small', '', ''",
            "'brand,Apple|Warranty,3 years', '', ''",
            "'brand,Apple|Warranty,5 years', '', ''",
            "'brand,Apple|Color,Red|Size,Medium', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1', 'Color=Red=2;Material=Plastic=1;Size=Medium=2;Weight=1.5kg=1'",
            "'brand,Apple|Color,Green|Weight,2kg', 'Color=Green=1;Size=Large=1;Weight=2kg=1', 'Color=Green=1;Size=Large=1;Weight=2kg=1'"
    })
    void givenFilterCounts_whenSortCounts_thenShouldReturnSortedCounts(String filter, String counts, String expectedSortedCounts) {
        String[] filters = filter.split("\\|");

        Map<String, Map<String, Long>> countsMap = parseExpectedCounts(counts);

        Map<String, Map<String, Long>> expectedSortedCountsMap = parseExpectedCounts(expectedSortedCounts);

        Map<String, Map<String, Long>> sortedCounts = productService.sortCounts(countsMap, filters);

        assertThat(sortedCounts).isEqualTo(expectedSortedCountsMap);
    }
}
