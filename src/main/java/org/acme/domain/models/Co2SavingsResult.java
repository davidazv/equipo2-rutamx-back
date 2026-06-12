package org.acme.domain.models;

import java.util.Map;

public class Co2SavingsResult {

    private String routeId;
    private String routeName;
    private String agencyId;
    private String agencyColor;
    private Double distanciaKm;
    private Double emisionesDieselTon;
    private Double emisionesElectricoTon;
    private Double ahorroTon;
    private Double score;
    private String prioridad;
    private Map<String, DetallesDia> detallesPorDia;

    public Co2SavingsResult() {
        // intentionally empty
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyColor() { return agencyColor; }
    public void setAgencyColor(String agencyColor) { this.agencyColor = agencyColor; }

    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }

    public Double getEmisionesDieselTon() { return emisionesDieselTon; }
    public void setEmisionesDieselTon(Double emisionesDieselTon) { this.emisionesDieselTon = emisionesDieselTon; }

    public Double getEmisionesElectricoTon() { return emisionesElectricoTon; }
    public void setEmisionesElectricoTon(Double emisionesElectricoTon) { this.emisionesElectricoTon = emisionesElectricoTon; }

    public Double getAhorroTon() { return ahorroTon; }
    public void setAhorroTon(Double ahorroTon) { this.ahorroTon = ahorroTon; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public Map<String, DetallesDia> getDetallesPorDia() { return detallesPorDia; }
    public void setDetallesPorDia(Map<String, DetallesDia> detallesPorDia) { this.detallesPorDia = detallesPorDia; }
}
