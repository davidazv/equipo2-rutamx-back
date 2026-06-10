package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.repository.BusModelRepository;
import org.acme.infrastructure.entities.BusModelEntity;
import org.acme.infrastructure.mapper.BusModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class BusModelRepositoryImpl implements BusModelRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<BusModel> findAll() {
        return entityManager
                .createQuery("SELECT b FROM BusModelEntity b ORDER BY b.id", BusModelEntity.class)
                .getResultList()
                .stream()
                .map(BusModelMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BusModel> findById(Long id) {
        BusModelEntity entity = entityManager.find(BusModelEntity.class, id);
        return Optional.ofNullable(entity).map(BusModelMapper::toDomain);
    }

    @Override
    public List<BusModel> findByFuelType(FuelType fuelType) {
        return entityManager
                .createQuery("SELECT b FROM BusModelEntity b WHERE b.fuelType = :fuelType ORDER BY b.id",
                        BusModelEntity.class)
                .setParameter("fuelType", fuelType)
                .getResultList()
                .stream()
                .map(BusModelMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BusModel create(BusModel model) {
        BusModelEntity entity = BusModelMapper.toEntity(model);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(entity);
        entityManager.flush();
        return BusModelMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public BusModel update(Long id, BusModel model) {
        BusModelEntity entity = entityManager.find(BusModelEntity.class, id);
        if (entity == null) throw new BusModelNotFoundException(id);

        entity.setName(model.getName());
        entity.setManufacturer(model.getManufacturer());
        entity.setFuelType(model.getFuelType());
        entity.setAutonomyKm(model.getAutonomyKm());
        entity.setPassengerCapacity(model.getPassengerCapacity());
        entity.setUnitCostUsd(model.getUnitCostUsd());
        entity.setBatteryCapacityKwh(model.getBatteryCapacityKwh());
        entity.setEnergyConsumptionKwhKm(model.getEnergyConsumptionKwhKm());
        entity.setFuelConsumptionLKm(model.getFuelConsumptionLKm());
        entity.setMaintenanceCostPerKm(model.getMaintenanceCostPerKm());
        entity.setCo2EmissionsGKm(model.getCo2EmissionsGKm());
        entity.setUpdatedAt(LocalDateTime.now());

        return BusModelMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BusModelEntity entity = entityManager.find(BusModelEntity.class, id);
        if (entity == null) throw new BusModelNotFoundException(id);
        entityManager.remove(entity);
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM BusModelEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<BusModel> items) {
        return BatchPersister.persistAll(entityManager, items, 50, item -> {
            BusModelEntity entity = BusModelMapper.toEntity(item);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            return entity;
        });
    }
}
