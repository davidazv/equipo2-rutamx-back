package org.acme.domain.models;

import java.util.List;

public class CooDashboard {

    private OperationalCounters operational;
    private List<PassengerTrendRow> trend;
    private List<HourlyTripRow> hourly;
    private List<DashboardAgencyRow> agencies;

    public CooDashboard() {}

    public CooDashboard(OperationalCounters operational,
                        List<PassengerTrendRow> trend,
                        List<HourlyTripRow> hourly,
                        List<DashboardAgencyRow> agencies) {
        this.operational = operational;
        this.trend = trend;
        this.hourly = hourly;
        this.agencies = agencies;
    }

    public OperationalCounters getOperational() { return operational; }
    public void setOperational(OperationalCounters operational) { this.operational = operational; }

    public List<PassengerTrendRow> getTrend() { return trend; }
    public void setTrend(List<PassengerTrendRow> trend) { this.trend = trend; }

    public List<HourlyTripRow> getHourly() { return hourly; }
    public void setHourly(List<HourlyTripRow> hourly) { this.hourly = hourly; }

    public List<DashboardAgencyRow> getAgencies() { return agencies; }
    public void setAgencies(List<DashboardAgencyRow> agencies) { this.agencies = agencies; }
}
