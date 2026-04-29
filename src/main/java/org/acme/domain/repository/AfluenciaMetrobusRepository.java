package org.acme.domain.repository;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.DayType;
import java.math.BigDecimal;
import java.util.List;

public interface AfluenciaMetrobusRepository {
    void deleteAll();
    int createAll(List<AfluenciaMetrobus> items);
    BigDecimal findAverageDailyDemand(String linea, DayType dayType);
}
