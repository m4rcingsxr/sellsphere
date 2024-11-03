package com.sellsphere.admin.export;

import com.sellsphere.common.entity.Brand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVExporterTest {

    private CSVExporter<Brand> csvExporter;
    private String[] headers;
    private Function<Brand, String[]> brandExtractor;

    @BeforeEach
    void setUp() {
        // Define the headers for the CSV
        headers = new String[]{"ID", "Name", "Logo"};

        // Define the extractor function that maps a Brand entity to a CSV line
        brandExtractor = brand -> new String[]{
                brand.getId().toString(),
                brand.getName(),
                brand.getLogo()
        };

        // Initialize the CSVExporter with headers and the extractor function
        csvExporter = new CSVExporter<>(headers, brandExtractor);
    }

    @Test
    void givenBrandList_whenExportToCSV_thenCorrectCSVIsWritten() throws IOException {
        // Given
        Supplier<List<Brand>> brandSupplier = () -> List.of(
                new Brand(1, "Brand1"),
                new Brand(2, "Brand2")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        csvExporter.export(brandSupplier, response);

        // Then
        String expectedCsvContent = """
                ID,Name,Logo
                1,Brand1,null
                2,Brand2,null
                """;

        assertEquals("text/csv", response.getContentType());
        assertEquals("attachment; filename=\"export.csv\"", response.getHeader("Content-Disposition"));
        assertEquals(expectedCsvContent, response.getContentAsString());
    }

    @Test
    void givenEmptyBrandList_whenExportToCSV_thenOnlyHeadersAreWritten() throws IOException {
        // Given
        Supplier<List<Brand>> brandSupplier = List::of; // Empty brand list
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        csvExporter.export(brandSupplier, response);

        // Then
        String expectedCsvContent = """
                ID,Name,Logo
                """;

        assertEquals("text/csv", response.getContentType());
        assertEquals("attachment; filename=\"export.csv\"", response.getHeader("Content-Disposition"));
        assertEquals(expectedCsvContent, response.getContentAsString());
    }
}
