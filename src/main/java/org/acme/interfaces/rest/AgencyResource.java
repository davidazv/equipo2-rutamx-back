package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.ListAgenciesWithColorsUseCase;
import org.acme.domain.repository.AgencyRepository;

@Path("/api/agencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgencyResource {

    private final AgencyRepository agencyRepository;
    private final ListAgenciesWithColorsUseCase listAgenciesWithColorsUseCase;

    @Inject
    public AgencyResource(AgencyRepository agencyRepository,
                          ListAgenciesWithColorsUseCase listAgenciesWithColorsUseCase) {
        this.agencyRepository = agencyRepository;
        this.listAgenciesWithColorsUseCase = listAgenciesWithColorsUseCase;
    }

    @GET
    public Response listAgencies() {
        return Response.ok(agencyRepository.findAll()).build();
    }

    @GET
    @Path("/with-colors")
    public Response listAgenciesWithColors() {
        return Response.ok(listAgenciesWithColorsUseCase.execute()).build();
    }
}
