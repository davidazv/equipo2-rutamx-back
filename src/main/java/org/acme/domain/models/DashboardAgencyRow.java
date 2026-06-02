package org.acme.domain.models;

public class DashboardAgencyRow {

    private String agencyId;
    private String agencyName;

    public DashboardAgencyRow() {}

    public DashboardAgencyRow(String agencyId, String agencyName) {
        this.agencyId = agencyId;
        this.agencyName = agencyName;
    }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
}
