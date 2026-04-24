package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.repository.BusModelRepository;
import org.acme.infrastructure.entities.BusModelEntity;
import org.acme.infrastructure.mapper.BusModelMapper;

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
}
