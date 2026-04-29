package org.acme.infrastructure.mapper;

import org.acme.domain.models.Frequency;
import org.acme.infrastructure.entities.FrequencyEntity;
import org.hibernate.Hibernate;

public class FrequencyMapper {

    private FrequencyMapper() {}

    public static Frequency toDomain(FrequencyEntity entity) {
        if (entity == null) return null;
        Frequency frequency = new Frequency();
        frequency.setStartTime(entity.getStartTime());
        frequency.setEndTime(entity.getEndTime());
        frequency.setHeadwaySecs(entity.getHeadwaySecs());
        frequency.setExactTimes(entity.getExactTimes());

        if (entity.getTrip() != null && Hibernate.isInitialized(entity.getTrip())) {
            frequency.setTripId(entity.getTrip().getTripId());
        }

        return frequency;
    }

    public static FrequencyEntity toEntity(Frequency frequency) {
        if (frequency == null) return null;
        FrequencyEntity entity = new FrequencyEntity();
        entity.setStartTime(frequency.getStartTime());
        entity.setEndTime(frequency.getEndTime());
        entity.setHeadwaySecs(frequency.getHeadwaySecs());
        entity.setExactTimes(frequency.getExactTimes());
        return entity;
    }
}
