package org.acme.domain.models;

import java.util.List;

public class CmoDashboard {

    private List<CmoDashboardRouteRow> routes;
    private List<DashboardAgencyRow> agencies;

    public CmoDashboard() {
        // intentionally empty
    }

    public CmoDashboard(List<CmoDashboardRouteRow> routes, List<DashboardAgencyRow> agencies) {
        this.routes = routes;
        this.agencies = agencies;
    }

    public List<CmoDashboardRouteRow> getRoutes() { return routes; }
    public void setRoutes(List<CmoDashboardRouteRow> routes) { this.routes = routes; }

    public List<DashboardAgencyRow> getAgencies() { return agencies; }
    public void setAgencies(List<DashboardAgencyRow> agencies) { this.agencies = agencies; }
}
