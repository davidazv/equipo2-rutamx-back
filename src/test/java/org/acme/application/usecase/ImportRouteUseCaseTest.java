package org.acme.application.usecase;

import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Route;
import org.acme.domain.repository.AgencyRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.domain.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportRouteUseCaseTest {

    private RouteRepository routeRepository;
    private TripRepository tripRepository;
    private StopTimeRepository stopTimeRepository;
    private FrequencyRepository frequencyRepository;
    private AgencyRepository agencyRepository;
    private RouteColorAssigner routeColorAssigner;
    private CsvParser csvParser;
    private ImportRouteUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        tripRepository = mock(TripRepository.class);
        stopTimeRepository = mock(StopTimeRepository.class);
        frequencyRepository = mock(FrequencyRepository.class);
        agencyRepository = mock(AgencyRepository.class);
        routeColorAssigner = mock(RouteColorAssigner.class);
        csvParser = mock(CsvParser.class);
        when(agencyRepository.findAllIds()).thenReturn(Set.of("AGENCY1"));
        useCase = new ImportRouteUseCase(routeRepository, tripRepository,
                stopTimeRepository, frequencyRepository, agencyRepository,
                routeColorAssigner, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Route> routes = List.of(buildRoute("R1", "FF0000"));
        stubParser(routes, 1, Collections.emptyList());
        when(routeRepository.createAll(routes)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("routes", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Route> routes = List.of(buildRoute("R1", "FF0000"), buildRoute("R2", "00FF00"));
        stubParser(routes, 3, List.of("Fila 3: bad type"));
        when(routeRepository.createAll(routes)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldCascadeDeleteInCorrectOrder() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(routeRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        InOrder inOrder = inOrder(frequencyRepository, stopTimeRepository,
                tripRepository, routeRepository);
        inOrder.verify(frequencyRepository).deleteAll();
        inOrder.verify(stopTimeRepository).deleteAll();
        inOrder.verify(tripRepository).deleteAll();
        inOrder.verify(routeRepository).deleteAll();
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(routeRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"route_id", "agency_id", "route_short_name",
                "route_long_name", "route_type", "route_color", "route_text_color"},
                captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(routeRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void executeShouldAssignMissingColorsBeforeInsert() {
        Route withColor = buildRoute("R1", "FF0000");
        Route noColor = buildRoute("R2", null);
        List<Route> routes = new ArrayList<>(List.of(withColor, noColor));
        stubParser(routes, 2, Collections.emptyList());
        when(routeColorAssigner.assignColor(anySet())).thenReturn("D40D0D");
        when(routeRepository.createAll(routes)).thenReturn(2);

        useCase.execute(dummyStream());

        assertEquals("D40D0D", noColor.getRouteColor());
        verify(routeColorAssigner).assignColor(anySet());
    }

    @Test
    void executeShouldNotOverwriteExistingColors() {
        Route withColor = buildRoute("R1", "FF0000");
        List<Route> routes = new ArrayList<>(List.of(withColor));
        stubParser(routes, 1, Collections.emptyList());
        when(routeRepository.createAll(routes)).thenReturn(1);

        useCase.execute(dummyStream());

        assertEquals("FF0000", withColor.getRouteColor());
        verify(routeColorAssigner, never()).assignColor(anySet());
    }

    private Route buildRoute(String id, String color) {
        Route r = new Route();
        r.setRouteId(id);
        r.setAgencyId("AGENCY1");
        r.setRouteShortName("R");
        r.setRouteLongName("Route " + id);
        r.setRouteType(3);
        r.setRouteColor(color);
        return r;
    }

    private void stubParser(List<Route> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Route> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
