package com.sellsphere.admin.export;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implements the DataExporter interface to provide Excel file exporting
 * functionality using the Apache POI library.
 * This class is responsible for generating an Excel spreadsheet (.xlsx) from
 * a list of entities and writing it to an HTTP response.
 * Entities are transformed into Excel format using a provided extractor
 * function that converts each entity into a row within the spreadsheet.
 *
 * @param <T> the type of the entity to be exported
 */
public class ExcelExporter<T> implements DataExporter<T> {
    private final String[] headers;
    private final Function<T, String[]> extractor;

    /**
     * Constructs a new ExcelExporter with specified headers and an extractor function.
     *
     * @param headers an array of strings representing the column headers of the Excel sheet
     * @param extractor a function that takes an entity of type T and returns an array of strings
     *                  representing its values in the order of the headers, to be used in each cell
     */
    public ExcelExporter(String[] headers, Function<T, String[]> extractor) {
        this.headers = headers;
        this.extractor = extractor;
    }

    /**
     * Exports a list of entities to Excel format (.xlsx) and writes it to the provided HttpServletResponse.
     * Each entity is converted into a row in the Excel sheet using the extractor function.
     *
     * @param entitiesSupplier a supplier that provides the list of entities to be exported
     * @param response the HttpServletResponse to which the Excel output will be written
     * @throws IOException if an I/O error occurs during writing to the response's output stream
     */
    @Override
    public void export(Supplier<List<T>> entitiesSupplier,
                       HttpServletResponse response) throws IOException {

        // Set the content type and headers for an Excel file download
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml" +
                        ".sheet");
        response.setHeader("Content-Disposition",
                           "attachment; filename=\"export.xlsx\""
        );

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Populate the header cells
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Write data
            List<T> entities = entitiesSupplier.get();
            int rowNum = 1;
            for (T entity : entities) {
                Row row = sheet.createRow(rowNum++);
                String[] values = extractor.apply(entity);

                for (int i = 0; i < values.length; i++) {
                    row.createCell(i).setCellValue(values[i]);
                }
            }

            // Automatically adjust column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the output to the response's output stream
            workbook.write(response.getOutputStream());
        }
    }
}