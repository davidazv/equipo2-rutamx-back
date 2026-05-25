package org.acme.domain.models;

import java.util.List;

public class HourlyFrequencyStats {

    private List<HourlyOccupancy> occupancyByHour;
    private List<HourlyBusDemand> busDemand;

    public HourlyFrequencyStats() {}

    public HourlyFrequencyStats(List<HourlyOccupancy> occupancyByHour, List<HourlyBusDemand> busDemand) {
        this.occupancyByHour = occupancyByHour;
        this.busDemand = busDemand;
    }

    public List<HourlyOccupancy> getOccupancyByHour() { return occupancyByHour; }
    public void setOccupancyByHour(List<HourlyOccupancy> occupancyByHour) { this.occupancyByHour = occupancyByHour; }

    public List<HourlyBusDemand> getBusDemand() { return busDemand; }
    public void setBusDemand(List<HourlyBusDemand> busDemand) { this.busDemand = busDemand; }
}
