package org.acme.domain.repository;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.AfluenciaResumen;
import java.util.List;
import java.util.Map;

public interface AfluenciaMetrobusRepository {
    void deleteAll();
    int createAll(List<AfluenciaMetrobus> items);
    List<AfluenciaResumen> findGroupedByLineaAndDow();
    /**
     * Returns the average daily passenger demand grouped by day type for a given linea.
     * Keys: "weekday", "saturday", "sunday". Empty map if no data found.
     * Used by HU12 (RecommendBusModelUseCase).
     */
    Map<String, Double> findAvgDemandByLinea(String linea);
}
