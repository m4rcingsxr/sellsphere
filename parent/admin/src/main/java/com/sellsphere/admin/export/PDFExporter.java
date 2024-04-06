package com.sellsphere.admin.export;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implements the DataExporter interface to provide PDF exporting
 * functionality using the iText library.
 * This class is responsible for generating a PDF document from a list of
 * entities, transforming each entity
 * into a row within a table in the PDF document. Entities are processed
 * using a provided extractor function,
 * which converts each entity into a string array representing its fields.
 *
 * @param <T> the type of entity to be exported
 */
public class PDFExporter<T> implements DataExporter<T> {
    private final String[] headers;
    private final Function<T, String[]> extractor;

    /**
     * Constructs a new PDFExporter with specified headers and an extractor
     * function.
     *
     * @param headers   an array of strings representing the column headers
     *                  of the PDF table
     * @param extractor a function that takes an entity of type T and returns
     *                  an array of strings
     *                  representing its values in the order of the headers
     */
    public PDFExporter(String[] headers, Function<T, String[]> extractor) {
        this.headers = headers;
        this.extractor = extractor;
    }

    /**
     * Exports a list of entities to a PDF format and writes it to the
     * provided HttpServletResponse.
     * The method creates a PDF document with a table that includes headers
     * and rows corresponding to
     * entities' values extracted via the extractor function.
     *
     * @param entitiesSupplier a supplier that provides the list of entities
     *                         to be exported
     * @param response         the HttpServletResponse to which the PDF
     *                         output will be written
     * @throws IOException if an I/O error occurs during writing to the
     *                     response's output stream
     */
    @Override
    public void export(Supplier<List<T>> entitiesSupplier, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"export.pdf\"");

        try (PdfWriter writer = new PdfWriter(
                response.getOutputStream()); PdfDocument pdf = new PdfDocument(writer);

             // Set the document to use A4 landscape orientation
             Document document = new Document(pdf, PageSize.A4.rotate())) {

            // Adjust the width of the table to match the page width
            // Assuming you want equal width for simplicity
            float[] columnWidths = new float[headers.length];

            for (int i = 0; i < headers.length; i++) {

                // Equal width distribution
                columnWidths[i] = 1;
            }
            Table table = new Table(columnWidths);

            // Adjust the table width
            table.setWidth(
                    PageSize.A4.rotate().getWidth() - document.getLeftMargin() - document.getRightMargin());

            // Adding headers
            for (String header : headers) {
                table.addHeaderCell(
                        new Paragraph(header).setBold().setTextAlignment(TextAlignment.CENTER));
            }

            // Adding content
            List<T> entities = entitiesSupplier.get();
            for (T entity : entities) {
                String[] values = extractor.apply(entity);
                for (String value : values) {
                    table.addCell(new Paragraph(value));
                }
            }

            document.add(table);
        }
    }
}