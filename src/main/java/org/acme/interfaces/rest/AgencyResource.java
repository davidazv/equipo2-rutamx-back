package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.ListAgenciesWithColorsUseCase;
import org.acme.domain.repository.AgencyRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/agencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agencias", description = "Consulta de agencias de transporte GTFS")
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
    @Operation(summary = "Listar agencias",
        description = "Devuelve todas las agencias cargadas desde el GTFS. Página: /[rol]/map. **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponse(responseCode = "200", description = "Lista de agencias")
    public Response listAgencies() {
        return Response.ok(agencyRepository.findAll()).build();
    }

    @GET
    @Path("/with-colors")
    @Operation(summary = "Listar agencias con colores",
        description = "Devuelve agencias incluyendo el color de línea asignado. Página: /[rol]/map (mapa interactivo). **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponse(responseCode = "200", description = "Lista de agencias con información de color")
    public Response listAgenciesWithColors() {
        return Response.ok(listAgenciesWithColorsUseCase.execute()).build();
    }
}
