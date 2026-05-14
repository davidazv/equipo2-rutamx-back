package org.acme.domain.repository;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.models.DayType;
import java.math.BigDecimal;
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
    /** Used by HU11 (RecommendBusCountUseCase). */
    BigDecimal findAverageDailyDemand(String linea, DayType dayType);
}
