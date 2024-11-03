package com.sellsphere.admin.export;

import com.sellsphere.common.entity.Brand;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelExporterTest {

    private ExcelExporter<Brand> excelExporter;
    private String[] headers;
    private Function<Brand, String[]> brandExtractor;

    @BeforeEach
    void setUp() {
        // Define the headers for the Excel sheet
        headers = new String[]{"ID", "Name", "Logo"};

        // Define the extractor function that maps a Brand entity to a String[] (for each row)
        brandExtractor = brand -> new String[]{
                brand.getId().toString(),
                brand.getName(),
                brand.getLogo()
        };

        // Initialize the ExcelExporter with headers and the extractor function
        excelExporter = new ExcelExporter<>(headers, brandExtractor);
    }

    @Test
    void givenBrandList_whenExportToExcel_thenCorrectExcelIsWritten() throws IOException {
        // Given
        Supplier<List<Brand>> brandSupplier = () -> List.of(
                createBrand(1, "Brand1", "logo1.png"),
                createBrand(2, "Brand2", "logo2.png")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        excelExporter.export(brandSupplier, response);

        // Then
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertEquals("attachment; filename=\"export.xlsx\"", response.getHeader("Content-Disposition"));

        // Parse the Excel content and verify
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            var sheet = workbook.getSheetAt(0);

            // Verify header row
            assertEquals("ID", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Name", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Logo", sheet.getRow(0).getCell(2).getStringCellValue());

            // Verify data rows
            assertEquals("1", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Brand1", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("logo1.png", sheet.getRow(1).getCell(2).getStringCellValue());

            assertEquals("2", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Brand2", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals("logo2.png", sheet.getRow(2).getCell(2).getStringCellValue());
        }
    }

    @Test
    void givenEmptyBrandList_whenExportToExcel_thenOnlyHeadersAreWritten() throws IOException {
        // Given
        Supplier<List<Brand>> brandSupplier = List::of;
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        excelExporter.export(brandSupplier, response);

        // Then
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertEquals("attachment; filename=\"export.xlsx\"", response.getHeader("Content-Disposition"));

        // Parse the Excel content and verify
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            var sheet = workbook.getSheetAt(0);

            // Verify header row
            assertEquals("ID", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Name", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Logo", sheet.getRow(0).getCell(2).getStringCellValue());

            // Ensure no other rows exist
            assertEquals(1, sheet.getPhysicalNumberOfRows());
        }
    }

    private Brand createBrand(int id, String name, String logo) {
        Brand brand = new Brand(id);
        brand.setName(name);
        brand.setLogo(logo);
        return brand;
    }
}
