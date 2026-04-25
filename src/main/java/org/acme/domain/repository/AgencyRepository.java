package org.acme.domain.repository;

import org.acme.domain.models.Agency;

import java.util.List;

public interface AgencyRepository {
    List<Agency> findAll();
}
