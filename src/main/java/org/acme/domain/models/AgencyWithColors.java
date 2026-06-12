package org.acme.domain.models;

import java.util.List;

public class AgencyWithColors {

    private String agencyId;
    private String agencyName;
    private String agencyColor;
    private List<String> sampleRouteColors;
    private boolean multiColor;
    private int routeCount;

    public AgencyWithColors() {
        // intentionally empty
    }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getAgencyColor() { return agencyColor; }
    public void setAgencyColor(String agencyColor) { this.agencyColor = agencyColor; }

    public List<String> getSampleRouteColors() { return sampleRouteColors; }
    public void setSampleRouteColors(List<String> sampleRouteColors) { this.sampleRouteColors = sampleRouteColors; }

    public boolean isMultiColor() { return multiColor; }
    public void setMultiColor(boolean multiColor) { this.multiColor = multiColor; }

    public int getRouteCount() { return routeCount; }
    public void setRouteCount(int routeCount) { this.routeCount = routeCount; }
}
