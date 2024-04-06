package com.sellsphere.admin.export;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class ExportUtil {

    public static <T> void export(String format, Supplier<List<T>> entitiesSupplier, String[] headers, Function<T, String[]> extractor, HttpServletResponse response)
            throws IOException {
        DataExporter<T> exporter = switch (format.toLowerCase()) {
            case "csv" -> new CSVExporter<>(headers, extractor);
            case "pdf" -> new PDFExporter<>(headers, extractor);
            case "excel" -> new ExcelExporter<>(headers, extractor);
            default -> throw new IllegalArgumentException(
                    "Unsupported export format: " + format);
        };

        exporter.export(entitiesSupplier, response);
    }

}
