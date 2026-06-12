package org.acme.domain.models;

public class TripsByDayResult {

    private String routeId;
    private String routeName;
    private String agencyColor;
    private int monday;
    private int tuesday;
    private int wednesday;
    private int thursday;
    private int friday;
    private int saturday;
    private int sunday;
    private int totalSemanal;
    private Double demandaDiariaPromedio;
    private String calidadDatos;

    public TripsByDayResult() {
        // intentionally empty
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

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

    public int getTotalSemanal() { return totalSemanal; }
    public void setTotalSemanal(int totalSemanal) { this.totalSemanal = totalSemanal; }

    public Double getDemandaDiariaPromedio() { return demandaDiariaPromedio; }
    public void setDemandaDiariaPromedio(Double demandaDiariaPromedio) { this.demandaDiariaPromedio = demandaDiariaPromedio; }

    public String getCalidadDatos() { return calidadDatos; }
    public void setCalidadDatos(String calidadDatos) { this.calidadDatos = calidadDatos; }
}
