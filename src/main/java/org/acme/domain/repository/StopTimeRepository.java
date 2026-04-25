package org.acme.domain.repository;

import org.acme.domain.models.StopTime;
import java.util.List;

public interface StopTimeRepository {
    void deleteAll();
    int createAll(List<StopTime> items);
}
