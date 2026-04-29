package org.acme.domain.repository;

import org.acme.domain.models.Trip;
import java.util.List;

public interface TripRepository {
    List<Trip> findAll();
    void deleteAll();
    int createAll(List<Trip> items);
}
