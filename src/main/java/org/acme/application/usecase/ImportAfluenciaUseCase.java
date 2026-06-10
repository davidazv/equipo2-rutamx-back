package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.AfluenciaMetrobusRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

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

        List<AfluenciaMetrobus> deduped = ImportSupport.dedupBy(result.getItems(),
                a -> a.getFecha() + "|" + a.getLinea() + "|" + a.getTipoPago());
        int imported = afluenciaMetrobusRepository.createAll(deduped);
        return new CsvImportResult("afluencia_metrobus", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private AfluenciaMetrobus mapRow(String[] row) {
        for (String cell : row) {
            if (cell.trim().isEmpty()) return null;
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(row[0].trim());
        } catch (DateTimeParseException e) {
            return null;
        }
        BigDecimal afluenciaVal = new BigDecimal(row[5].trim());
        if (afluenciaVal.compareTo(BigDecimal.ZERO) <= 0) return null;

        String linea = row[3].trim().toLowerCase();
        linea = Normalizer.normalize(linea, Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "");

        AfluenciaMetrobus afluencia = new AfluenciaMetrobus();
        afluencia.setFecha(fecha);
        afluencia.setMes(row[1].trim());
        afluencia.setAnio(Short.parseShort(row[2].trim()));
        afluencia.setLinea(linea);
        afluencia.setTipoPago(row[4].trim());
        afluencia.setAfluencia(afluenciaVal);
        return afluencia;
    }
}
