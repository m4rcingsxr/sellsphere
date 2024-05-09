package com.sellsphere.client.product;

import com.sellsphere.client.CSVParser;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ProductFilterParser {

    public static List<Filter> parseProductFilters(String[] filters) {
        // Validate filters length
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("Filters are required in format: [filter...] ; filter='name,value'");
        }

        // Parse filters
        boolean isSingle = Arrays.stream(filters).noneMatch(filter -> filter.contains(","));

        if (isSingle) {
            return List.of(parseFilter(String.join(",", filters)));
        } else {
            return Arrays.stream(filters)
                    .map(ProductFilterParser::parseFilter)
                    .toList();
        }
    }

    public static Filter parseFilter(String csvFilter) {

        // allow filter values to have commas when they are surrounded with single quote
        String[] filters = CSVParser.parseCSV(csvFilter);

        // Validate if each entry contains at least 2 values
        if (filters.length != 2) {
            throw new IllegalArgumentException(csvFilter + " is incorrect. Each filter must have two values: filter='name,value'");
        }

        String name = filters[0].trim();
        String value = filters[1].trim();

        if (name.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Filter name and value cannot be empty.");
        }

        return new Filter(name, value);
    }

}
