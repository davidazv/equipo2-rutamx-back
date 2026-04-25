package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.repository.AgencyRepository;

@Path("/api/agencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgencyResource {

    private final AgencyRepository agencyRepository;

    @Inject
    public AgencyResource(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    @GET
    public Response listAgencies() {
        return Response.ok(agencyRepository.findAll()).build();
    }
}
