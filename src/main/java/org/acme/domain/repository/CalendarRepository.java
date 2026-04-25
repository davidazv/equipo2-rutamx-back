package org.acme.domain.repository;

import org.acme.domain.models.Calendar;
import java.util.List;

public interface CalendarRepository {
    List<Calendar> findAll();
    void deleteAll();
    int createAll(List<Calendar> items);
}
