package org.acme.infrastructure.mapper;

import org.acme.domain.models.Trip;
import org.acme.infrastructure.entities.TripEntity;
import org.hibernate.Hibernate;

public class TripMapper {

    private TripMapper() {}

    public static Trip toDomain(TripEntity entity) {
        if (entity == null) return null;
        Trip trip = new Trip();
        trip.setTripId(entity.getTripId());
        trip.setShapeId(entity.getShapeId());
        trip.setTripHeadsign(entity.getTripHeadsign());
        trip.setTripShortName(entity.getTripShortName());
        trip.setDirectionId(entity.getDirectionId());

        if (entity.getRoute() != null && Hibernate.isInitialized(entity.getRoute())) {
            trip.setRouteId(entity.getRoute().getRouteId());
        }

        if (entity.getCalendar() != null && Hibernate.isInitialized(entity.getCalendar())) {
            trip.setServiceId(entity.getCalendar().getServiceId());
        }

        return trip;
    }

    public static TripEntity toEntity(Trip trip) {
        if (trip == null) return null;
        TripEntity entity = new TripEntity();
        entity.setTripId(trip.getTripId());
        entity.setShapeId(trip.getShapeId());
        entity.setTripHeadsign(trip.getTripHeadsign());
        entity.setTripShortName(trip.getTripShortName());
        entity.setDirectionId(trip.getDirectionId());
        return entity;
    }
}
