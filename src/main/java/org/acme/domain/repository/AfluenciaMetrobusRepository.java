package org.acme.domain.repository;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.AfluenciaResumen;
import java.util.List;

public interface AfluenciaMetrobusRepository {
    void deleteAll();
    int createAll(List<AfluenciaMetrobus> items);
    List<AfluenciaResumen> findGroupedByLineaAndDow();
}
