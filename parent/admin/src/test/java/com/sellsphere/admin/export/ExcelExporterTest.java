package com.sellsphere.admin.export;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.sellsphere.admin.category.TestCategoryHelper;
import com.sellsphere.admin.export.ExcelExporter;
import com.sellsphere.common.entity.Category;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

class ExcelExporterTest {

    private ExcelExporter<Category> excelExporter;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        String[] headers = {"ID", "Name", "Alias", "Enabled"};
        excelExporter = new ExcelExporter<>(headers, this::extractCategoryData);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void testExport() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));

        List<Category> categories = TestCategoryHelper.generateRootCategories();
        excelExporter.export(TestCategoryHelper::generateRootCategories, response);

        // Verify the content of the generated Excel file
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()));

        // Verify content using helper method
        ExportTestHelper.assertExcelContent(workbook, categories);

        workbook.close();
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