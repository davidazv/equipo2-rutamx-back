package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.domain.models.Agency;
import org.acme.domain.repository.AgencyRepository;
import org.acme.infrastructure.entities.AgencyEntity;
import org.acme.infrastructure.mapper.AgencyMapper;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgencyRepositoryImpl implements AgencyRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Agency> findAll() {
        return entityManager
                .createQuery("SELECT a FROM AgencyEntity a ORDER BY a.agencyName", AgencyEntity.class)
                .getResultList()
                .stream()
                .map(AgencyMapper::toDomain)
                .collect(Collectors.toList());
    }
}
