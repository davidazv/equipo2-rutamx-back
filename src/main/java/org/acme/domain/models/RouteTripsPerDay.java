package org.acme.domain.models;

public class RouteTripsPerDay {

    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private String agencyId;
    private String agencyColor;
    private int monday;
    private int tuesday;
    private int wednesday;
    private int thursday;
    private int friday;
    private int saturday;
    private int sunday;

    public RouteTripsPerDay() {
        // intentionally empty
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyColor() { return agencyColor; }
    public void setAgencyColor(String agencyColor) { this.agencyColor = agencyColor; }

    public int getMonday() { return monday; }
    public void setMonday(int monday) { this.monday = monday; }

    public int getTuesday() { return tuesday; }
    public void setTuesday(int tuesday) { this.tuesday = tuesday; }

    public int getWednesday() { return wednesday; }
    public void setWednesday(int wednesday) { this.wednesday = wednesday; }

    public int getThursday() { return thursday; }
    public void setThursday(int thursday) { this.thursday = thursday; }

    public int getFriday() { return friday; }
    public void setFriday(int friday) { this.friday = friday; }

    public int getSaturday() { return saturday; }
    public void setSaturday(int saturday) { this.saturday = saturday; }

    public int getSunday() { return sunday; }
    public void setSunday(int sunday) { this.sunday = sunday; }
}
