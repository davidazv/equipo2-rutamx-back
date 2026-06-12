package org.acme.domain.models;

public class Agency {

    private String agencyId;
    private String agencyName;
    private String agencyUrl;
    private String agencyTimezone;
    private String agencyLang;
    private String agencyColor;

    public Agency() {
        // intentionally empty
    }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getAgencyUrl() { return agencyUrl; }
    public void setAgencyUrl(String agencyUrl) { this.agencyUrl = agencyUrl; }

    public String getAgencyTimezone() { return agencyTimezone; }
    public void setAgencyTimezone(String agencyTimezone) { this.agencyTimezone = agencyTimezone; }

    public String getAgencyLang() { return agencyLang; }
    public void setAgencyLang(String agencyLang) { this.agencyLang = agencyLang; }

    public String getAgencyColor() { return agencyColor; }
    public void setAgencyColor(String agencyColor) { this.agencyColor = agencyColor; }
}
