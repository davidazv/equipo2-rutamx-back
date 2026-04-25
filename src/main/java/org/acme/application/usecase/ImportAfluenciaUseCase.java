package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.AfluenciaMetrobusRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

@ApplicationScoped
public class ImportAfluenciaUseCase {

    private final AfluenciaMetrobusRepository afluenciaMetrobusRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportAfluenciaUseCase(AfluenciaMetrobusRepository afluenciaMetrobusRepository,
                                  CsvParser csvParser) {
        this.afluenciaMetrobusRepository = afluenciaMetrobusRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across delete + bulk insert.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"fecha", "mes", "anio", "linea",
                "tipo_pago", "afluencia"};

        CsvParser.ParseResult<AfluenciaMetrobus> result =
                csvParser.parse(csvFile, headers, this::mapRow);

        afluenciaMetrobusRepository.deleteAll();

        int imported = afluenciaMetrobusRepository.createAll(result.getItems());
        return new CsvImportResult("afluencia_metrobus", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private AfluenciaMetrobus mapRow(String[] row) {
        AfluenciaMetrobus afluencia = new AfluenciaMetrobus();
        afluencia.setFecha(LocalDate.parse(row[0].trim()));
        afluencia.setMes(row[1].trim());
        afluencia.setAnio(Short.parseShort(row[2].trim()));
        afluencia.setLinea(row[3].trim());
        afluencia.setTipoPago(row[4].trim());
        afluencia.setAfluencia(new BigDecimal(row[5].trim()));
        return afluencia;
    }
}
