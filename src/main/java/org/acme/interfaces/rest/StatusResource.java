package org.acme.interfaces.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sistema", description = "Health check de la API")
public class StatusResource {

    @GET
    @Operation(summary = "Health check",
        description = "Verifica que el servidor esté en línea. Sin página específica en el frontend. **Roles:** público")
    @APIResponse(responseCode = "200", description = "Servidor disponible")
    public Map<String, String> status() {
        return Map.of("status", "ok");
    }
}
