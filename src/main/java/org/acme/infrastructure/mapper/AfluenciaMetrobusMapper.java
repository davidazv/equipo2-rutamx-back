package org.acme.infrastructure.mapper;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.infrastructure.entities.AfluenciaMetrobusEntity;

public class AfluenciaMetrobusMapper {

    private AfluenciaMetrobusMapper() {}

    public static AfluenciaMetrobus toDomain(AfluenciaMetrobusEntity entity) {
        if (entity == null) return null;
        AfluenciaMetrobus model = new AfluenciaMetrobus();
        model.setFecha(entity.getFecha());
        model.setMes(entity.getMes());
        model.setAnio(entity.getAnio());
        model.setLinea(entity.getLinea());
        model.setTipoPago(entity.getTipoPago());
        model.setAfluencia(entity.getAfluencia());
        return model;
    }

    public static AfluenciaMetrobusEntity toEntity(AfluenciaMetrobus model) {
        if (model == null) return null;
        AfluenciaMetrobusEntity entity = new AfluenciaMetrobusEntity();
        entity.setFecha(model.getFecha());
        entity.setMes(model.getMes());
        entity.setAnio(model.getAnio());
        entity.setLinea(model.getLinea());
        entity.setTipoPago(model.getTipoPago());
        entity.setAfluencia(model.getAfluencia());
        return entity;
    }
}
