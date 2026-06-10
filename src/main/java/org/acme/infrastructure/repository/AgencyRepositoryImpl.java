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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                "SELECT a.agency_id, a.agency_name, a.agency_color, r.route_color, " +
                "(SELECT COUNT(*) FROM routes r2 WHERE r2.agency_id = a.agency_id) AS route_count " +
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
                a.setRouteCount(((Number) row[4]).intValue());
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
    public Set<String> findAllIds() {
        List<String> ids = entityManager
                .createQuery("SELECT a.agencyId FROM AgencyEntity a", String.class)
                .getResultList();
        return new HashSet<>(ids);
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM AgencyEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Agency> items) {
        return BatchPersister.persistAll(entityManager, items, 50, AgencyMapper::toEntity);
    }
}
