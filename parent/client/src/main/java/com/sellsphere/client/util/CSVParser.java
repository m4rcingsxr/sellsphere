package com.sellsphere.client.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for parsing CSV (Comma Separated Values) strings.
 * This class provides a method to parse a CSV string into an array of strings,
 * handling values enclosed in single quotes.
 */
@UtilityClass
public class CSVParser {

    /**
     * Parses a CSV string into an array of strings.
     *
     * This method processes the input string to split it into individual values,
     * handling cases where values are enclosed in single quotes to allow commas within values.
     *
     * @param input the CSV string to be parsed
     *              Example: "'value1', 'value,2', value3"
     *
     * @return an array of strings containing the individual values
     *         Example: ["value1", "value,2", "value3"]
     *
     * This method allow to have entries with commas - but whole entry must be surrounded by single quotes
     */
    public static String[] parseCSV(String input) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '\'') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }
}