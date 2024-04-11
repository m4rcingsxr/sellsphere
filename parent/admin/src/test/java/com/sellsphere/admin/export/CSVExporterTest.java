package com.sellsphere.admin.export;

import com.sellsphere.admin.category.TestCategoryHelper;
import com.sellsphere.common.entity.Category;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CSVExporterTest {
    private CSVExporter<Category> csvExporter;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        String[] headers = {"ID", "Name", "Alias", "Enabled"};
        csvExporter = new CSVExporter<>(headers, this::extractCategoryData);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void testExport() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(response.getWriter()).thenReturn(new PrintWriter(outputStream));

        csvExporter.export(TestCategoryHelper::generateRootCategories, response);

        String csvContent = outputStream.toString();

        // Verify CSV content using helper method
        List<Category> categories = TestCategoryHelper.generateRootCategories();
        ExportTestHelper.assertCSVContent(csvContent, categories);
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