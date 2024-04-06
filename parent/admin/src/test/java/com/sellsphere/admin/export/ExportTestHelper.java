package com.sellsphere.admin.export;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.sellsphere.common.entity.Category;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UtilityClass
class ExportTestHelper {

    public static void assertExcelContent(Workbook workbook, List<Category> categories) {
        for (int i = 0; i < categories.size(); i++) {
            Row dataRow = workbook.getSheetAt(0).getRow(i + 1);
            Category category = categories.get(i);

            assertEquals(category.getId().toString(), dataRow.getCell(0).getStringCellValue());
            assertEquals(category.getName(), dataRow.getCell(1).getStringCellValue());
            assertEquals(category.getAlias(), dataRow.getCell(2).getStringCellValue());
            assertEquals(String.valueOf(category.isEnabled()),
                         dataRow.getCell(3).getStringCellValue()
            );
        }
    }

    public static void assertPDFContent(byte[] pdfBytes, List<Category> categories)
            throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            String pdfText = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(1));
            for (Category category : categories) {
                assertTrue(pdfText.contains(category.getId().toString()));
                assertTrue(pdfText.contains(category.getName()));
                assertTrue(pdfText.contains(category.getAlias()));
                assertTrue(pdfText.contains(String.valueOf(category.isEnabled())));
            }
        }
    }

    public static void assertCSVContent(String csvContent, List<Category> categories) {
        String[] lines = csvContent.split("\n");
        for (int i = 1; i < lines.length; i++) { // Start from 1 to skip header
            String[] values = lines[i].split(",");
            Category category = categories.get(i - 1);

            assertEquals(category.getId().toString(), values[0]);
            assertEquals(category.getName(), values[1]);
            assertEquals(category.getAlias(), values[2]);
            assertEquals(String.valueOf(category.isEnabled()), values[3]);
        }
    }
}