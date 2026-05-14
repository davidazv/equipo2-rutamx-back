package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
    public Optional<BusModel> findByName(String name) {
        return entityManager
                .createQuery("SELECT b FROM BusModelEntity b WHERE b.name = :name", BusModelEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .map(BusModelMapper::toDomain);
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
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM BusModelEntity").executeUpdate();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        BusModelEntity entity = entityManager.find(BusModelEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
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
    public int createAll(List<BusModel> items) {
        int count = 0;
        for (BusModel item : items) {
            BusModelEntity entity = BusModelMapper.toEntity(item);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(entity);
            if (++count % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }

    @Override
    @Transactional
    public BusModel update(BusModel model) {
        BusModelEntity entity = entityManager.find(BusModelEntity.class, model.getId());
        if (entity == null) {
            return null;
        }
        if (model.getName() != null) entity.setName(model.getName());
        if (model.getAutonomyKm() != null) entity.setAutonomyKm(model.getAutonomyKm());
        if (model.getPassengerCapacity() != null) entity.setPassengerCapacity(model.getPassengerCapacity());
        if (model.getUnitCostUsd() != null) entity.setUnitCostUsd(model.getUnitCostUsd());
        entity.setUpdatedAt(LocalDateTime.now());
        entityManager.flush();
        return BusModelMapper.toDomain(entity);
    }
}
