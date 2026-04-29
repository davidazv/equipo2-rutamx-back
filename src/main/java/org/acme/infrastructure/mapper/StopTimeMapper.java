package org.acme.infrastructure.mapper;

import org.acme.domain.models.StopTime;
import org.acme.infrastructure.entities.StopTimeEntity;
import org.hibernate.Hibernate;

public class StopTimeMapper {

    private StopTimeMapper() {}

    public static StopTime toDomain(StopTimeEntity entity) {
        if (entity == null) return null;
        StopTime stopTime = new StopTime();
        stopTime.setStopSequence(entity.getStopSequence());
        stopTime.setArrivalTime(entity.getArrivalTime());
        stopTime.setDepartureTime(entity.getDepartureTime());
        stopTime.setTimepoint(entity.getTimepoint());

        if (entity.getTrip() != null && Hibernate.isInitialized(entity.getTrip())) {
            stopTime.setTripId(entity.getTrip().getTripId());
        }

        if (entity.getStop() != null && Hibernate.isInitialized(entity.getStop())) {
            stopTime.setStopId(entity.getStop().getStopId());
        }

        return stopTime;
    }

    public static StopTimeEntity toEntity(StopTime stopTime) {
        if (stopTime == null) return null;
        StopTimeEntity entity = new StopTimeEntity();
        entity.setStopSequence(stopTime.getStopSequence());
        entity.setArrivalTime(stopTime.getArrivalTime());
        entity.setDepartureTime(stopTime.getDepartureTime());
        entity.setTimepoint(stopTime.getTimepoint());
        return entity;
    }
}
