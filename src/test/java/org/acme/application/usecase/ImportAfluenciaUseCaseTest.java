package org.acme.application.usecase;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportAfluenciaUseCaseTest {

    private AfluenciaMetrobusRepository afluenciaRepository;
    private CsvParser csvParser;
    private ImportAfluenciaUseCase useCase;

    @BeforeEach
    void setUp() {
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportAfluenciaUseCase(afluenciaRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<AfluenciaMetrobus> items = List.of(buildAfluencia());
        stubParser(items, 1, Collections.emptyList());
        when(afluenciaRepository.createAll(items)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("afluencia_metrobus", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        AfluenciaMetrobus a1 = buildAfluencia();
        AfluenciaMetrobus a2 = buildAfluencia();
        a2.setFecha(LocalDate.of(2026, 3, 16));
        List<AfluenciaMetrobus> items = List.of(a1, a2);
        stubParser(items, 3, List.of("Fila 3: bad date"));
        when(afluenciaRepository.createAll(items)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldDeleteBeforeInsert() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(afluenciaRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        var inOrder = inOrder(afluenciaRepository);
        inOrder.verify(afluenciaRepository).deleteAll();
        inOrder.verify(afluenciaRepository).createAll(anyList());
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(afluenciaRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"fecha", "mes", "anio", "linea",
                "tipo_pago", "afluencia"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(afluenciaRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldParseFieldsCorrectly() {
        CsvParser realParser = new CsvParser();
        ImportAfluenciaUseCase realUseCase = new ImportAfluenciaUseCase(afluenciaRepository, realParser);
        when(afluenciaRepository.createAll(anyList())).thenReturn(1);

        String csv = "fecha,mes,anio,linea,tipo_pago,afluencia\n"
                + "2026-03-15,Marzo,2026,Linea 1,Tarjeta,15000.50\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        realUseCase.execute(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AfluenciaMetrobus>> captor = ArgumentCaptor.forClass(List.class);
        verify(afluenciaRepository).createAll(captor.capture());
        AfluenciaMetrobus a = captor.getValue().get(0);
        assertEquals(LocalDate.of(2026, 3, 15), a.getFecha());
        assertEquals("Marzo", a.getMes());
        assertEquals(2026, a.getAnio());
        assertEquals("linea 1", a.getLinea());
        assertEquals("Tarjeta", a.getTipoPago());
        assertEquals(new BigDecimal("15000.50"), a.getAfluencia());
    }

    private AfluenciaMetrobus buildAfluencia() {
        AfluenciaMetrobus a = new AfluenciaMetrobus();
        a.setFecha(LocalDate.of(2026, 3, 15));
        a.setMes("Marzo");
        a.setAnio((short) 2026);
        a.setLinea("Linea 1");
        a.setTipoPago("Tarjeta");
        a.setAfluencia(new BigDecimal("15000"));
        return a;
    }

    private void stubParser(List<AfluenciaMetrobus> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<AfluenciaMetrobus> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
