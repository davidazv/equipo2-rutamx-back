package org.acme.domain.repository;

import org.acme.domain.models.Frequency;
import java.util.List;

public interface FrequencyRepository {
    void deleteAll();
    int createAll(List<Frequency> items);
    Double findAverageHeadwayByRouteShortName(String routeShortName);
}
