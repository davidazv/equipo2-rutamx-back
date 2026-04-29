package org.acme.domain.models;

import java.time.LocalDate;

public class Calendar {

    private String serviceId;
    private byte monday;
    private byte tuesday;
    private byte wednesday;
    private byte thursday;
    private byte friday;
    private byte saturday;
    private byte sunday;
    private LocalDate startDate;
    private LocalDate endDate;

    public Calendar() {}

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public byte getMonday() { return monday; }
    public void setMonday(byte monday) { this.monday = monday; }

    public byte getTuesday() { return tuesday; }
    public void setTuesday(byte tuesday) { this.tuesday = tuesday; }

    public byte getWednesday() { return wednesday; }
    public void setWednesday(byte wednesday) { this.wednesday = wednesday; }

    public byte getThursday() { return thursday; }
    public void setThursday(byte thursday) { this.thursday = thursday; }

    public byte getFriday() { return friday; }
    public void setFriday(byte friday) { this.friday = friday; }

    public byte getSaturday() { return saturday; }
    public void setSaturday(byte saturday) { this.saturday = saturday; }

    public byte getSunday() { return sunday; }
    public void setSunday(byte sunday) { this.sunday = sunday; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
