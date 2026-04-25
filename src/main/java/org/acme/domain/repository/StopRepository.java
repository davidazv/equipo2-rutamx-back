package org.acme.domain.repository;

import org.acme.domain.models.Stop;
import java.util.List;

public interface StopRepository {
    List<Stop> findAll();
    void deleteAll();
    int createAll(List<Stop> items);
}
