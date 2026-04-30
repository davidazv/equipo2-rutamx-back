package org.acme.domain.repository;

import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;

import java.util.List;
import java.util.Optional;

public interface BusModelRepository {
    List<BusModel> findAll();
    Optional<BusModel> findById(Long id);
    Optional<BusModel> findByName(String name);
    List<BusModel> findByFuelType(FuelType fuelType);
    void deleteAll();
    void deleteById(Long id);
    int createAll(List<BusModel> items);
    BusModel create(BusModel model);
    BusModel update(BusModel model);
}
