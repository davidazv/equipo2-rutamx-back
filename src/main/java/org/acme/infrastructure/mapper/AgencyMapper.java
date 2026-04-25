package org.acme.infrastructure.mapper;

import org.acme.domain.models.Agency;
import org.acme.infrastructure.entities.AgencyEntity;

public class AgencyMapper {

    private AgencyMapper() {}

    public static Agency toDomain(AgencyEntity entity) {
        Agency agency = new Agency();
        agency.setAgencyId(entity.getAgencyId());
        agency.setAgencyName(entity.getAgencyName());
        return agency;
    }
}
