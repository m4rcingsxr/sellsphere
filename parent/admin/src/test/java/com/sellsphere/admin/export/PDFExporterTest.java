package com.sellsphere.admin.export;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.sellsphere.common.entity.Brand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PDFExporterTest {

    private PDFExporter<Brand> pdfExporter;
    private String[] headers;
    private Function<Brand, String[]> brandExtractor;

    @BeforeEach
    void setUp() {
        // Define the headers for the PDF table
        headers = new String[]{"ID", "Name", "Logo"};

        // Define the extractor function that maps a Brand entity to a String[] (for each row)
        brandExtractor = brand -> new String[]{
                brand.getId().toString(),
                brand.getName(),
                brand.getLogo()
        };

        // Initialize the PDFExporter with headers and the extractor function
        pdfExporter = new PDFExporter<>(headers, brandExtractor);
    }

    @Test
    void givenBrandList_whenExportToPDF_thenCorrectPDFIsWritten() throws IOException {
        // Given
        Supplier<List<Brand>> brandSupplier = () -> List.of(
                createBrand(1, "Brand1", "logo1.png"),
                createBrand(2, "Brand2", "logo2.png")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        pdfExporter.export(brandSupplier, response);

        // Then
        assertEquals("application/pdf", response.getContentType());
        assertEquals("attachment; filename=\"export.pdf\"", response.getHeader("Content-Disposition"));

        // Parse the PDF content and verify
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(response.getContentAsByteArray()));
             PdfDocument pdfDocument = new PdfDocument(reader)) {

            // Extract text from the first page of the PDF
            String pdfContent = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(1));

            // Verify that the headers are present in the PDF
            assertTrue(pdfContent.contains("ID"));
            assertTrue(pdfContent.contains("Name"));
            assertTrue(pdfContent.contains("Logo"));

            // Verify that the data rows are present in the PDF
            assertTrue(pdfContent.contains("1"));
            assertTrue(pdfContent.contains("Brand1"));
            assertTrue(pdfContent.contains("logo1.png"));

            assertTrue(pdfContent.contains("2"));
            assertTrue(pdfContent.contains("Brand2"));
            assertTrue(pdfContent.contains("logo2.png"));
        }
    }

    @Test
    void givenEmptyBrandList_whenExportToPDF_thenOnlyHeadersAreWritten() throws IOException {
        // Given
        Supplier<List<Brand>> brandSupplier = List::of; // Empty brand list
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        pdfExporter.export(brandSupplier, response);

        // Then
        assertEquals("application/pdf", response.getContentType());
        assertEquals("attachment; filename=\"export.pdf\"", response.getHeader("Content-Disposition"));

        // Parse the PDF content and verify
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(response.getContentAsByteArray()));
             PdfDocument pdfDocument = new PdfDocument(reader)) {

            // Extract text from the first page of the PDF
            String pdfContent = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(1));

            // Verify that only the headers are present in the PDF
            assertTrue(pdfContent.contains("ID"));
            assertTrue(pdfContent.contains("Name"));
            assertTrue(pdfContent.contains("Logo"));

            // Ensure no other data rows exist
            assertFalse(pdfContent.contains("1"));
            assertFalse(pdfContent.contains("Brand1"));
            assertFalse(pdfContent.contains("logo1.png"));
        }
    }

    // Helper method to create a brand without using non-existing constructors
    private Brand createBrand(int id, String name, String logo) {
        Brand brand = new Brand(id);
        brand.setName(name);
        brand.setLogo(logo);
        return brand;
    }
}
