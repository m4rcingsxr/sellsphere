package com.sellsphere.admin.export;

import com.sellsphere.admin.category.TestCategoryHelper;
import com.sellsphere.common.entity.Category;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PDFExporterTest {

    private PDFExporter<Category> pdfExporter;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        String[] headers = {"ID", "Name", "Alias", "Enabled"};
        pdfExporter = new PDFExporter<>(headers, this::extractCategoryData);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void testExport() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));

        pdfExporter.export(TestCategoryHelper::generateRootCategories, response);

        byte[] pdfBytes = outputStream.toByteArray();

        // Verify PDF content using helper method
        ExportTestHelper.assertPDFContent(pdfBytes, TestCategoryHelper.generateRootCategories());
    }

    private String[] extractCategoryData(Category category) {
        return new String[] {
                category.getId().toString(),
                category.getName(),
                category.getAlias(),
                String.valueOf(category.isEnabled())
        };
    }
}