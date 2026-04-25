package org.acme.infrastructure.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "agency")
public class AgencyEntity {

    @Id
    @Column(name = "agency_id", length = 50)
    private String agencyId;

    @Column(name = "agency_name", nullable = false, length = 150)
    private String agencyName;

    @Column(name = "agency_url", length = 255)
    private String agencyUrl;

    @Column(name = "agency_timezone", nullable = false, length = 50)
    private String agencyTimezone;

    @Column(name = "agency_lang", length = 10)
    private String agencyLang;

    @Column(name = "agency_color", length = 10)
    private String agencyColor;

    public AgencyEntity() {}

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
