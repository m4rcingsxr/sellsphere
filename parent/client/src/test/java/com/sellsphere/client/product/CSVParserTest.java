package com.sellsphere.client.product;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CSVParserTest {

    @ParameterizedTest
    @MethodSource("provideCsvData")
    void testParseCSV(String input, String[] expected) {
        String[] result = CSVParser.parseCSV(input);
        assertArrayEquals(expected, result);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> provideCsvData() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("a,b,c", new String[]{"a", "b", "c"}),
                org.junit.jupiter.params.provider.Arguments.of("a,'b,b',c", new String[]{"a", "b,b", "c"}),
                org.junit.jupiter.params.provider.Arguments.of("'a,a','b,b','c,c'", new String[]{"a,a", "b,b", "c,c"}),
                org.junit.jupiter.params.provider.Arguments.of("'a,a',b,'c,c'", new String[]{"a,a", "b", "c,c"})
        );
    }
}