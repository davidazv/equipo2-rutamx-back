package org.acme.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private CsvParser csvParser;

    @BeforeEach
    void setUp() {
        csvParser = new CsvParser();
    }

    @Test
    void parseShouldReturnItemsForValidCsv() {
        String csv = "name,age\nAlice,30\nBob,25\n";
        InputStream input = toStream(csv);
        String[] headers = {"name", "age"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers, row -> row[0].trim());

        assertEquals(2, result.getItems().size());
        assertEquals("Alice", result.getItems().get(0));
        assertEquals("Bob", result.getItems().get(1));
        assertEquals(2, result.getTotalRows());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void parseShouldReturnErrorForEmptyFile() {
        String csv = "";
        InputStream input = toStream(csv);
        String[] headers = {"name"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers, row -> row[0]);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalRows());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("vacío"));
    }

    @Test
    void parseShouldReturnErrorForWrongHeaders() {
        String csv = "wrong,headers\nAlice,30\n";
        InputStream input = toStream(csv);
        String[] headers = {"name", "age"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers, row -> row[0]);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalRows());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Encabezados incorrectos"));
    }

    @Test
    void parseShouldAllowExtraHeaders() {
        String csv = "name,age,extra\nAlice,30,x\n";
        InputStream input = toStream(csv);
        String[] headers = {"name", "age"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers, row -> row[0].trim());

        assertEquals(1, result.getItems().size());
        assertEquals("Alice", result.getItems().get(0));
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void parseShouldRejectFewerHeaders() {
        String csv = "name\nAlice\n";
        InputStream input = toStream(csv);
        String[] headers = {"name", "age"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers, row -> row[0]);

        assertTrue(result.getItems().isEmpty());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Encabezados incorrectos"));
    }

    @Test
    void parseShouldCollectRowErrors() {
        String csv = "value\n10\nbad\n20\n";
        InputStream input = toStream(csv);
        String[] headers = {"value"};
        Function<String[], Integer> mapper = row -> {
            int val = Integer.parseInt(row[0].trim());
            if (val < 0) throw new RuntimeException("negative");
            return val;
        };

        // "bad" will throw NumberFormatException
        CsvParser.ParseResult<Integer> result = csvParser.parse(input, headers, mapper);

        assertEquals(2, result.getItems().size());
        assertEquals(3, result.getTotalRows());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Fila 2"));
    }

    @Test
    void parseShouldCapErrorsAt50() {
        StringBuilder csv = new StringBuilder("value\n");
        for (int i = 0; i < 60; i++) {
            csv.append("bad\n");
        }
        InputStream input = toStream(csv.toString());
        String[] headers = {"value"};

        CsvParser.ParseResult<Integer> result = csvParser.parse(input, headers,
                row -> Integer.parseInt(row[0].trim()));

        assertEquals(0, result.getItems().size());
        assertEquals(60, result.getTotalRows());
        assertEquals(50, result.getErrors().size());
    }

    @Test
    void parseShouldSkipNullItemsFromMapper() {
        String csv = "value\n10\nskip\n20\n";
        InputStream input = toStream(csv);
        String[] headers = {"value"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers,
                row -> row[0].trim().equals("skip") ? null : row[0].trim());

        assertEquals(2, result.getItems().size());
        assertEquals("10", result.getItems().get(0));
        assertEquals("20", result.getItems().get(1));
        assertEquals(3, result.getTotalRows());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void parseShouldBeCaseInsensitiveOnHeaders() {
        String csv = "NAME,AGE\nAlice,30\n";
        InputStream input = toStream(csv);
        String[] headers = {"name", "age"};

        CsvParser.ParseResult<String> result = csvParser.parse(input, headers, row -> row[0].trim());

        assertEquals(1, result.getItems().size());
        assertEquals("Alice", result.getItems().get(0));
        assertTrue(result.getErrors().isEmpty());
    }

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
