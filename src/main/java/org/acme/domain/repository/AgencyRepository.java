package org.acme.domain.repository;

import org.acme.domain.models.Agency;
import org.acme.domain.models.AgencyWithColors;

import java.util.List;
import java.util.Set;

public interface AgencyRepository {
    List<Agency> findAll();
    Set<String> findAllIds();
    List<AgencyWithColors> findAllWithColorInfo();
    void deleteAll();
    int createAll(List<Agency> items);
}
