package org.acme.infrastructure.mapper;

import org.acme.domain.models.Shape;
import org.acme.infrastructure.entities.ShapeEntity;

public class ShapeMapper {

    private ShapeMapper() {}

    public static Shape toDomain(ShapeEntity entity) {
        if (entity == null) return null;
        Shape shape = new Shape();
        shape.setShapeId(entity.getShapeId());
        shape.setShapePtLat(entity.getShapePtLat());
        shape.setShapePtLon(entity.getShapePtLon());
        shape.setShapePtSequence(entity.getShapePtSequence());
        shape.setShapeDistTraveled(entity.getShapeDistTraveled());
        return shape;
    }

    public static ShapeEntity toEntity(Shape shape) {
        if (shape == null) return null;
        ShapeEntity entity = new ShapeEntity();
        entity.setShapeId(shape.getShapeId());
        entity.setShapePtLat(shape.getShapePtLat());
        entity.setShapePtLon(shape.getShapePtLon());
        entity.setShapePtSequence(shape.getShapePtSequence());
        entity.setShapeDistTraveled(shape.getShapeDistTraveled());
        return entity;
    }
}
