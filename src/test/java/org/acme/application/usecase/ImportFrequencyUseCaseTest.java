package org.acme.application.usecase;

import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Frequency;
import org.acme.domain.repository.FrequencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportFrequencyUseCaseTest {

    private FrequencyRepository frequencyRepository;
    private CsvParser csvParser;
    private ImportFrequencyUseCase useCase;

    @BeforeEach
    void setUp() {
        frequencyRepository = mock(FrequencyRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportFrequencyUseCase(frequencyRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Frequency> freqs = List.of(buildFrequency("T1"));
        stubParser(freqs, 1, Collections.emptyList());
        when(frequencyRepository.createAll(freqs)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("frequencies", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Frequency> freqs = List.of(buildFrequency("T1"), buildFrequency("T2"));
        stubParser(freqs, 3, List.of("Fila 3: bad secs"));
        when(frequencyRepository.createAll(freqs)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldDeleteBeforeInsert() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(frequencyRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        var inOrder = inOrder(frequencyRepository);
        inOrder.verify(frequencyRepository).deleteAll();
        inOrder.verify(frequencyRepository).createAll(anyList());
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(frequencyRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"trip_id", "start_time", "end_time",
                "headway_secs", "exact_times"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(frequencyRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldHandleOptionalExactTimes() {
        CsvParser realParser = new CsvParser();
        ImportFrequencyUseCase realUseCase = new ImportFrequencyUseCase(frequencyRepository, realParser);
        when(frequencyRepository.createAll(anyList())).thenReturn(1);

        String csv = "trip_id,start_time,end_time,headway_secs,exact_times\n"
                + "T1,06:00:00,22:00:00,600,\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        realUseCase.execute(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Frequency>> captor = ArgumentCaptor.forClass(List.class);
        verify(frequencyRepository).createAll(captor.capture());
        Frequency freq = captor.getValue().get(0);
        assertNull(freq.getExactTimes());
        assertEquals(600, freq.getHeadwaySecs());
    }

    private Frequency buildFrequency(String tripId) {
        Frequency f = new Frequency();
        f.setTripId(tripId);
        f.setStartTime("06:00:00");
        f.setEndTime("22:00:00");
        f.setHeadwaySecs(600);
        return f;
    }

    private void stubParser(List<Frequency> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Frequency> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
