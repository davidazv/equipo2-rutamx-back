package org.acme.infrastructure.mapper;

import org.acme.domain.models.Calendar;
import org.acme.infrastructure.entities.CalendarEntity;

public class CalendarMapper {

    private CalendarMapper() {}

    public static Calendar toDomain(CalendarEntity entity) {
        if (entity == null) return null;
        Calendar calendar = new Calendar();
        calendar.setServiceId(entity.getServiceId());
        calendar.setMonday(entity.getMonday());
        calendar.setTuesday(entity.getTuesday());
        calendar.setWednesday(entity.getWednesday());
        calendar.setThursday(entity.getThursday());
        calendar.setFriday(entity.getFriday());
        calendar.setSaturday(entity.getSaturday());
        calendar.setSunday(entity.getSunday());
        calendar.setStartDate(entity.getStartDate());
        calendar.setEndDate(entity.getEndDate());
        return calendar;
    }

    public static CalendarEntity toEntity(Calendar calendar) {
        if (calendar == null) return null;
        CalendarEntity entity = new CalendarEntity();
        entity.setServiceId(calendar.getServiceId());
        entity.setMonday(calendar.getMonday());
        entity.setTuesday(calendar.getTuesday());
        entity.setWednesday(calendar.getWednesday());
        entity.setThursday(calendar.getThursday());
        entity.setFriday(calendar.getFriday());
        entity.setSaturday(calendar.getSaturday());
        entity.setSunday(calendar.getSunday());
        entity.setStartDate(calendar.getStartDate());
        entity.setEndDate(calendar.getEndDate());
        return entity;
    }
}
