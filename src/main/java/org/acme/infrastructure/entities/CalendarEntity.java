package org.acme.infrastructure.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "calendar")
public class CalendarEntity {

    @Id
    @Column(name = "service_id", length = 50)
    private String serviceId;

    @Column(name = "monday", nullable = false)
    private Byte monday;

    @Column(name = "tuesday", nullable = false)
    private Byte tuesday;

    @Column(name = "wednesday", nullable = false)
    private Byte wednesday;

    @Column(name = "thursday", nullable = false)
    private Byte thursday;

    @Column(name = "friday", nullable = false)
    private Byte friday;

    @Column(name = "saturday", nullable = false)
    private Byte saturday;

    @Column(name = "sunday", nullable = false)
    private Byte sunday;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public CalendarEntity() {}

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public Byte getMonday() { return monday; }
    public void setMonday(Byte monday) { this.monday = monday; }

    public Byte getTuesday() { return tuesday; }
    public void setTuesday(Byte tuesday) { this.tuesday = tuesday; }

    public Byte getWednesday() { return wednesday; }
    public void setWednesday(Byte wednesday) { this.wednesday = wednesday; }

    public Byte getThursday() { return thursday; }
    public void setThursday(Byte thursday) { this.thursday = thursday; }

    public Byte getFriday() { return friday; }
    public void setFriday(Byte friday) { this.friday = friday; }

    public Byte getSaturday() { return saturday; }
    public void setSaturday(Byte saturday) { this.saturday = saturday; }

    public Byte getSunday() { return sunday; }
    public void setSunday(Byte sunday) { this.sunday = sunday; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
