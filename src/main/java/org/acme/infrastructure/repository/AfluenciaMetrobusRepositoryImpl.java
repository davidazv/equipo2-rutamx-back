package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.infrastructure.entities.AfluenciaMetrobusEntity;
import org.acme.infrastructure.mapper.AfluenciaMetrobusMapper;

import java.util.List;

@ApplicationScoped
public class AfluenciaMetrobusRepositoryImpl implements AfluenciaMetrobusRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM afluencia_metrobus").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<AfluenciaMetrobus> items) {
        int count = 0;
        for (AfluenciaMetrobus item : items) {
            AfluenciaMetrobusEntity entity = AfluenciaMetrobusMapper.toEntity(item);
            entityManager.persist(entity);
            if (++count % 500 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
