package org.acme.domain.repository;

import org.acme.domain.models.Agency;
import org.acme.domain.models.AgencyWithColors;

import java.util.List;

public interface AgencyRepository {
    List<Agency> findAll();
    List<AgencyWithColors> findAllWithColorInfo();
    void deleteAll();
    int createAll(List<Agency> items);
}
