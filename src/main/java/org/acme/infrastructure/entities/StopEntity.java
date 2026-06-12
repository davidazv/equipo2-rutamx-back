package org.acme.infrastructure.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stops")
public class StopEntity {

    @Id
    @Column(name = "stop_id", length = 100)
    private String stopId;

    @Column(name = "stop_name", nullable = false, length = 255)
    private String stopName;

    @Column(name = "stop_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal stopLat;

    @Column(name = "stop_lon", nullable = false, precision = 10, scale = 7)
    private BigDecimal stopLon;

    @Column(name = "zone_id", length = 50)
    private String zoneId;

    @Column(name = "wheelchair_boarding")
    private Byte wheelchairBoarding;

    public StopEntity() {
        // intentionally empty
    }

    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public BigDecimal getStopLat() { return stopLat; }
    public void setStopLat(BigDecimal stopLat) { this.stopLat = stopLat; }

    public BigDecimal getStopLon() { return stopLon; }
    public void setStopLon(BigDecimal stopLon) { this.stopLon = stopLon; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public Byte getWheelchairBoarding() { return wheelchairBoarding; }
    public void setWheelchairBoarding(Byte wheelchairBoarding) { this.wheelchairBoarding = wheelchairBoarding; }
}
