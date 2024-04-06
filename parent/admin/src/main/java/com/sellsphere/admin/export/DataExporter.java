package com.sellsphere.admin.export;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * The DataExporter interface defines a generic contract for exporting data
 * into various formats.
 * Implementations of this interface should handle the specifics of
 * generating the data export
 * based on a list of entities provided by a supplier and writing the result
 * to an HTTP response.
 *
 * @param <T> the type of the entity to be exported
 */
public interface DataExporter<T> {

    /**
     * Exports a list of entities to a format specified by the implementing class,
     * writing the output to the provided HTTP response.
     *
     * @param entitiesSupplier a supplier that provides the list of entities to be exported
     * @param response the HTTP response to which the export output will be written
     * @throws IOException if an I/O error occurs during writing to the response
     */
    void export(Supplier<List<T>> entitiesSupplier,
                HttpServletResponse response) throws IOException;
}