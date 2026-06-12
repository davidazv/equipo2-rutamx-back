package org.acme.domain.models;

import java.math.BigDecimal;

public class Stop {

    private String stopId;
    private String stopName;
    private BigDecimal stopLat;
    private BigDecimal stopLon;
    private String zoneId;
    private Byte wheelchairBoarding;

    public Stop() {
        // intentionally empty
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public BigDecimal getStopLat() {
        return stopLat;
    }

    public void setStopLat(BigDecimal stopLat) {
        this.stopLat = stopLat;
    }

    public BigDecimal getStopLon() {
        return stopLon;
    }

    public void setStopLon(BigDecimal stopLon) {
        this.stopLon = stopLon;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public Byte getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public void setWheelchairBoarding(Byte wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }
}
