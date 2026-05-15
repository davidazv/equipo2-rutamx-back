package org.acme.domain.models;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum DayType {
    WEEKDAY, SATURDAY, SUNDAY;

    public static DayType fromDate(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return SATURDAY;
        if (dow == DayOfWeek.SUNDAY) return SUNDAY;
        return WEEKDAY;
    }

    public static DayType fromString(String value) {
        if (value == null || value.isBlank()) return WEEKDAY;
        return switch (value.toLowerCase()) {
            case "weekday" -> WEEKDAY;
            case "saturday" -> SATURDAY;
            case "sunday" -> SUNDAY;
            default -> throw new IllegalArgumentException(
                    "Tipo de día inválido: " + value + ". Valores válidos: weekday, saturday, sunday");
        };
    }
}
