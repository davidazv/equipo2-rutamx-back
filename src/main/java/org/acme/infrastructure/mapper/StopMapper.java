package org.acme.infrastructure.mapper;

import org.acme.domain.models.Stop;
import org.acme.infrastructure.entities.StopEntity;

public class StopMapper {

    private StopMapper() {}

    public static Stop toDomain(StopEntity entity) {
        if (entity == null) return null;
        Stop stop = new Stop();
        stop.setStopId(entity.getStopId());
        stop.setStopName(entity.getStopName());
        stop.setStopLat(entity.getStopLat());
        stop.setStopLon(entity.getStopLon());
        stop.setZoneId(entity.getZoneId());
        stop.setWheelchairBoarding(entity.getWheelchairBoarding());
        return stop;
    }

    public static StopEntity toEntity(Stop stop) {
        if (stop == null) return null;
        StopEntity entity = new StopEntity();
        entity.setStopId(stop.getStopId());
        entity.setStopName(stop.getStopName());
        entity.setStopLat(stop.getStopLat());
        entity.setStopLon(stop.getStopLon());
        entity.setZoneId(stop.getZoneId());
        entity.setWheelchairBoarding(stop.getWheelchairBoarding());
        return entity;
    }
}
