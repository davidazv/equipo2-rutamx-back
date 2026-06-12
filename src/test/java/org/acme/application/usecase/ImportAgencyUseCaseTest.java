package org.acme.application.usecase;

import org.acme.domain.models.Agency;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportAgencyUseCaseTest {

    private AgencyRepository agencyRepository;
    private RouteRepository routeRepository;
    private TripRepository tripRepository;
    private StopTimeRepository stopTimeRepository;
    private FrequencyRepository frequencyRepository;
    private CsvParser csvParser;
    private ImportAgencyUseCase useCase;

    @BeforeEach
    void setUp() {
        agencyRepository = mock(AgencyRepository.class);
        routeRepository = mock(RouteRepository.class);
        tripRepository = mock(TripRepository.class);
        stopTimeRepository = mock(StopTimeRepository.class);
        frequencyRepository = mock(FrequencyRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportAgencyUseCase(agencyRepository, routeRepository,
                tripRepository, stopTimeRepository, frequencyRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Agency> agencies = List.of(buildAgency("AG1", "Agency One"));
        stubParser(agencies, 1, Collections.emptyList());
        when(agencyRepository.createAll(agencies)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("agency", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Agency> agencies = List.of(buildAgency("AG1", "One"), buildAgency("AG2", "Two"));
        List<String> errors = List.of("Fila 3: error");
        stubParser(agencies, 3, errors);
        when(agencyRepository.createAll(agencies)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
        assertEquals(1, result.getErrors().size());
        assertEquals("agency", result.getTableName());
    }

    @Test
    void executeShouldCascadeDeleteInCorrectOrder() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(agencyRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        InOrder inOrder = inOrder(frequencyRepository, stopTimeRepository,
                tripRepository, routeRepository, agencyRepository);
        inOrder.verify(frequencyRepository).deleteAll();
        inOrder.verify(stopTimeRepository).deleteAll();
        inOrder.verify(tripRepository).deleteAll();
        inOrder.verify(routeRepository).deleteAll();
        inOrder.verify(agencyRepository).deleteAll();
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(agencyRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> headersCaptor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), headersCaptor.capture(), any());
        String[] headers = headersCaptor.getValue();
        assertArrayEquals(new String[]{"agency_id", "agency_name", "agency_url",
                "agency_timezone", "agency_lang"}, headers);
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(agencyRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getTotalRows());
        assertEquals(0, result.getImportedRows());
        assertEquals(0, result.getSkippedRows());
    }

    private Agency buildAgency(String id, String name) {
        Agency a = new Agency();
        a.setAgencyId(id);
        a.setAgencyName(name);
        a.setAgencyUrl("http://test.com");
        a.setAgencyTimezone("America/Mexico_City");
        a.setAgencyLang("es");
        return a;
    }

    private void stubParser(List<Agency> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Agency> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
