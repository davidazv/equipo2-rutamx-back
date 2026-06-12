package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.models.User;
import org.acme.infrastructure.security.AuthContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/me")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Perfil", description = "Perfil del usuario autenticado")
public class MeResource {

    private final AuthContext authContext;

    @Inject
    public MeResource(AuthContext authContext) {
        this.authContext = authContext;
    }

    @GET
    @Operation(
        summary = "Perfil del usuario autenticado",
        description = "Devuelve id, email, nombre y rol del usuario que presenta el token. " +
                      "Accesible para ADMIN, CEO, COO y CMO. Usado por el login para la redirección por rol.")
    @APIResponse(responseCode = "200", description = "Perfil del usuario")
    @APIResponse(responseCode = "401", description = "Token ausente o inválido")
    @APIResponse(responseCode = "403", description = "Cuenta suspendida o rol no reconocido")
    public Response getMe() {
        User user = authContext.getUser();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("No autenticado").build();
        }
        return Response.ok(new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoleName()
        )).build();
    }

    public record MeResponse(Long id, String email, String firstName, String lastName, String roleName) {}
}
