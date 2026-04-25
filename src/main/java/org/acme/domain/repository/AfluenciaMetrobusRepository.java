package org.acme.domain.repository;

import org.acme.domain.models.AfluenciaMetrobus;
import java.util.List;

public interface AfluenciaMetrobusRepository {
    void deleteAll();
    int createAll(List<AfluenciaMetrobus> items);
}
