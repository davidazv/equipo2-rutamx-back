package org.acme.infrastructure.mapper;

import org.acme.domain.models.Agency;
import org.acme.infrastructure.entities.AgencyEntity;

public class AgencyMapper {

    private AgencyMapper() {}

    public static Agency toDomain(AgencyEntity entity) {
        Agency agency = new Agency();
        agency.setAgencyId(entity.getAgencyId());
        agency.setAgencyName(entity.getAgencyName());
        agency.setAgencyUrl(entity.getAgencyUrl());
        agency.setAgencyTimezone(entity.getAgencyTimezone());
        agency.setAgencyLang(entity.getAgencyLang());
        agency.setAgencyColor(entity.getAgencyColor());
        return agency;
    }

    public static AgencyEntity toEntity(Agency agency) {
        AgencyEntity entity = new AgencyEntity();
        entity.setAgencyId(agency.getAgencyId());
        entity.setAgencyName(agency.getAgencyName());
        entity.setAgencyUrl(agency.getAgencyUrl());
        entity.setAgencyTimezone(agency.getAgencyTimezone());
        entity.setAgencyLang(agency.getAgencyLang());
        entity.setAgencyColor(agency.getAgencyColor());
        return entity;
    }
}
