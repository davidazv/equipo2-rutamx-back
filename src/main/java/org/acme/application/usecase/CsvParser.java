package org.acme.application.usecase;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

@ApplicationScoped
public class CsvParser {

    private static final Logger log = Logger.getLogger(CsvParser.class.getName());
    private static final int MAX_ERRORS = 50;

    public <T> ParseResult<T> parse(InputStream input, String[] expectedHeaders,
                                     Function<String[], T> rowMapper) {
        List<T> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalRows = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                errors.add("El archivo está vacío");
                return new ParseResult<>(items, 0, errors);
            }

            String[] trimmedHeaders = Arrays.stream(headers)
                    .map(String::trim)
                    .toArray(String[]::new);

            if (!headersMatch(trimmedHeaders, expectedHeaders)) {
                errors.add("Encabezados incorrectos. Esperados: " + Arrays.toString(expectedHeaders)
                        + ", encontrados: " + Arrays.toString(trimmedHeaders));
                return new ParseResult<>(items, 0, errors);
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                totalRows++;
                try {
                    T item = rowMapper.apply(row);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    if (errors.size() < MAX_ERRORS) {
                        errors.add("Fila " + totalRows + ": " + e.getMessage());
                    }
                }
            }
        } catch (CsvValidationException | java.io.IOException e) {
            log.severe("Error parsing CSV: " + e.getMessage());
            errors.add("Error al leer el archivo CSV: " + e.getMessage());
        }

        return new ParseResult<>(items, totalRows, errors);
    }

    private boolean headersMatch(String[] actual, String[] expected) {
        if (actual.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equalsIgnoreCase(actual[i])) {
                return false;
            }
        }
        return true;
    }

    public static class ParseResult<T> {
        private final List<T> items;
        private final int totalRows;
        private final List<String> errors;

        public ParseResult(List<T> items, int totalRows, List<String> errors) {
            this.items = items;
            this.totalRows = totalRows;
            this.errors = errors;
        }

        public List<T> getItems() { return items; }
        public int getTotalRows() { return totalRows; }
        public List<String> getErrors() { return errors; }
    }
}
