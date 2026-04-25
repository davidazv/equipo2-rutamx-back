package org.acme.domain.repository;

import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;

import java.util.List;
import java.util.Optional;

public interface BusModelRepository {
    List<BusModel> findAll();
    Optional<BusModel> findById(Long id);
    List<BusModel> findByFuelType(FuelType fuelType);
    void deleteAll();
    int createAll(List<BusModel> items);
}
