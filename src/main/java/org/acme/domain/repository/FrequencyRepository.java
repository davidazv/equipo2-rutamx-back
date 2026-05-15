package org.acme.domain.repository;

import org.acme.domain.models.Frequency;
import java.util.List;
import java.util.Map;

public interface FrequencyRepository {
    void deleteAll();
    int createAll(List<Frequency> items);
    List<Frequency> findAll();
    Map<String, Integer> findAvgHeadwaySecsByRoute();
}
