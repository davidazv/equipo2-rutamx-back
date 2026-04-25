package org.acme.domain.repository;

import org.acme.domain.models.Shape;
import java.util.List;

public interface ShapeRepository {
    List<Shape> findAll();
    void deleteAll();
    int createAll(List<Shape> items);
}
