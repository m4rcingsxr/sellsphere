package com.sellsphere.admin.export;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implements the DataExporter interface to provide CSV file exporting functionality.
 * This class handles the generation of a CSV file from a list of entities and writes it
 * to an HTTP response. Entities are converted to CSV format using a provided extractor function.
 *
 * @param <T> the type of the entity to be exported
 */
public class CSVExporter<T> implements DataExporter<T>{
    private final String[] headers;
    private final Function<T, String[]> extractor;

    /**
     * Constructs a new CSVExporter with specified headers and an extractor function.
     *
     * @param headers an array of strings representing the column headers of the CSV file
     * @param extractor a function that takes an entity of type T and returns an array of strings
     *                  representing its values in the order of the headers
     */
    public CSVExporter(String[] headers, Function<T, String[]> extractor) {
        this.headers = headers;
        this.extractor = extractor;
    }

    /**
     * Exports a list of entities to a CSV format and writes it to the provided HttpServletResponse.
     * Each entity is converted to CSV format using the extractor function.
     *
     * @param entitiesSupplier a supplier that provides the list of entities to be exported
     * @param response the HttpServletResponse to which the CSV output will be written
     * @throws IOException if an I/O error occurs during writing to the response
     */
    @Override
    public void export(Supplier<List<T>> entitiesSupplier, HttpServletResponse response) throws IOException {

        // Set the content type and headers for a CSV file download
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            // Write the headers
            writeLine(writer, headers);

            // Retrieve entities using the supplied behavior
            List<T> entities = entitiesSupplier.get();

            // Write the data using the provided extractor function
            for (T entity : entities) {
                String[] values = extractor.apply(entity); // Use the extractor function
                writeLine(writer, values);
            }
        }
    }

    // Writes a single line to the CSV file, joining values with commas
    private void writeLine(PrintWriter writer, String[] values) {

        // Iterates over values to construct a line of CSV text
        for (int i = 0; i < values.length; i++) {
            writer.append(values[i]);
            if (i < values.length - 1) {
                writer.append(',');
            }
        }
        writer.append('\n');
    }
}