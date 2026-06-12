package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetCooDashboardUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

@Path("/api/coo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "COO", description = "Dashboard operativo consolidado para el COO")
public class CooResource {

    private static final Logger log = Logger.getLogger(CooResource.class.getName());

    private final GetCooDashboardUseCase getCooDashboardUseCase;

    @Inject
    public CooResource(GetCooDashboardUseCase getCooDashboardUseCase) {
        this.getCooDashboardUseCase = getCooDashboardUseCase;
    }

    @GET
    @Path("/dashboard")
    @Operation(summary = "Dashboard consolidado del COO",
        description = "Una sola llamada que regresa contadores operativos, tendencia de pasajeros por día de semana, viajes por hora y catálogo de agencias. Implementado vía sp_get_coo_dashboard. Página: /coo/dashboard. **Roles:** ADMIN, COO")
    @APIResponse(responseCode = "200", description = "Dashboard COO consolidado")
    @APIResponse(responseCode = "400", description = "Formato de fecha inválido (ISO YYYY-MM-DD)")
    @APIResponse(responseCode = "500", description = "Error inesperado")
    public Response getDashboard(
            @Parameter(description = "Fecha inicio (YYYY-MM-DD). Opcional.") @QueryParam("start") String startStr,
            @Parameter(description = "Fecha fin (YYYY-MM-DD). Opcional.")    @QueryParam("end")   String endStr) {
        LocalDate start;
        LocalDate end;
        try {
            start = (startStr == null || startStr.isBlank()) ? null : LocalDate.parse(startStr);
            end   = (endStr   == null || endStr.isBlank())   ? null : LocalDate.parse(endStr);
        } catch (DateTimeParseException e) {
            return Response.status(400).entity("Formato de fecha inválido. Use YYYY-MM-DD").build();
        }
        try {
            return Response.ok(getCooDashboardUseCase.execute(start, end)).build();
        } catch (Exception e) {
            log.severe("Error obteniendo dashboard COO: " + e.getMessage());
            return Response.serverError()
                    .entity("Error inesperado al obtener dashboard COO")
                    .build();
        }
    }
}
