package org.acme.domain.repository;

import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;

import java.util.List;
import java.util.Optional;

public interface BusModelRepository {
    List<BusModel> findAll();
    Optional<BusModel> findById(Long id);
    List<BusModel> findByFuelType(FuelType fuelType);
    BusModel create(BusModel model);
    BusModel update(Long id, BusModel model);
    void delete(Long id);
    void deleteAll();
    int createAll(List<BusModel> items);
}
