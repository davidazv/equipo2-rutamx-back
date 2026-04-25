package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Agency;
import org.acme.domain.models.AgencyWithColors;
import org.acme.domain.repository.AgencyRepository;
import org.acme.infrastructure.entities.AgencyEntity;
import org.acme.infrastructure.mapper.AgencyMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    @SuppressWarnings("unchecked")
    public List<AgencyWithColors> findAllWithColorInfo() {
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT a.agency_id, a.agency_name, a.agency_color, r.route_color " +
                "FROM agency a " +
                "LEFT JOIN routes r ON r.agency_id = a.agency_id AND r.route_color IS NOT NULL AND r.route_color != '' " +
                "ORDER BY a.agency_name, r.route_color"
        ).getResultList();

        Map<String, AgencyWithColors> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String agencyId = (String) row[0];
            AgencyWithColors awc = map.computeIfAbsent(agencyId, k -> {
                AgencyWithColors a = new AgencyWithColors();
                a.setAgencyId(agencyId);
                a.setAgencyName((String) row[1]);
                a.setAgencyColor((String) row[2]);
                a.setSampleRouteColors(new ArrayList<>());
                return a;
            });
            String routeColor = (String) row[3];
            if (routeColor != null && !routeColor.isEmpty()
                    && !awc.getSampleRouteColors().contains(routeColor)
                    && awc.getSampleRouteColors().size() < 5) {
                awc.getSampleRouteColors().add(routeColor);
            }
        }

        for (AgencyWithColors awc : map.values()) {
            awc.setMultiColor(awc.getSampleRouteColors().size() > 1);
        }

        return new ArrayList<>(map.values());
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM AgencyEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Agency> items) {
        int count = 0;
        for (Agency item : items) {
            AgencyEntity entity = AgencyMapper.toEntity(item);
            entityManager.persist(entity);
            if (++count % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
