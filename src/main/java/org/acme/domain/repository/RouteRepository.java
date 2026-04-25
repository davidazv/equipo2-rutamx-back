package org.acme.domain.repository;

import org.acme.domain.models.Route;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    List<Route> findAllWithDistance();
    Optional<Route> findByIdWithDistance(String routeId);
}
