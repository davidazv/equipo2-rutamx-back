package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.dto.CreateUserDto;
import org.acme.application.dto.ResetPasswordDto;
import org.acme.application.dto.UpdateUserDto;
import org.acme.application.exception.DuplicateEmailException;
import org.acme.application.exception.UserAlreadyActiveException;
import org.acme.application.exception.UserAlreadySuspendedException;
import org.acme.application.exception.UserNotFoundException;
import org.acme.application.usecase.ActivateUserUseCase;
import org.acme.application.usecase.CreateUserUseCase;
import org.acme.application.usecase.DeleteUserUseCase;
import org.acme.application.usecase.ExportUsersUseCase;
import org.acme.application.usecase.ResetPasswordUseCase;
import org.acme.application.usecase.SuspendUserUseCase;
import org.acme.application.usecase.UpdateUserUseCase;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.security.AuthContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.enterprise.context.RequestScoped;

import java.time.ZoneOffset;
import java.util.logging.Logger;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (solo ADMIN)")
public class UserResource {

    private static final Logger log = Logger.getLogger(UserResource.class.getName());

    private static final String ADMIN_ONLY = "Solo el rol ADMIN";
    private static final String ERROR_INESPERADO = "Error inesperado";
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final SuspendUserUseCase suspendUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final ExportUsersUseCase exportUsersUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final AuthContext authContext;

    // Exception to the no-direct-repo-in-resource rule: read-only list/get-one
    // do not justify a separate use case (per HU01-04 spec).
    private final UserRepository userRepository;

    @Inject
    public UserResource(CreateUserUseCase createUserUseCase,
                        UpdateUserUseCase updateUserUseCase,
                        DeleteUserUseCase deleteUserUseCase,
                        SuspendUserUseCase suspendUserUseCase,
                        ActivateUserUseCase activateUserUseCase,
                        ExportUsersUseCase exportUsersUseCase,
                        ResetPasswordUseCase resetPasswordUseCase,
                        AuthContext authContext,
                        UserRepository userRepository) {
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.suspendUserUseCase = suspendUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.exportUsersUseCase = exportUsersUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.authContext = authContext;
        this.userRepository = userRepository;
    }

    @GET
    @Operation(summary = "Listar usuarios (paginado)",
        description = "Devuelve usuarios paginados. Parámetros: page (>=0, default 0), size (1-200, default 50). Si no se envían se asume primera página de 50. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Página de usuarios con metadata (totalItems, totalPages, hasNext, hasPrevious)")
    public Response listUsers(
            @Parameter(description = "Número de página, 0-indexado", example = "0")
            @QueryParam("page") @DefaultValue("0") @PositiveOrZero int page,
            @Parameter(description = "Tamaño de página (1-200)", example = "50")
            @QueryParam("size") @DefaultValue("50") @Min(1) @Max(200) int size) {
        return Response.ok(userRepository.findPaginated(page, size)).build();
    }

    @GET
    @Path("/export")
    @Produces("text/csv")
    @Operation(summary = "Exportar usuarios en CSV",
        description = "Genera un archivo CSV con todos los usuarios. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Archivo CSV con los usuarios",
        content = @Content(mediaType = "text/csv"))
    @APIResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN")
    public Response exportUsers() {
        if (authContext.getUser() == null || !"ADMIN".equals(authContext.getUser().getRoleName())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ADMIN_ONLY + " puede exportar usuarios").build();
        }
        String today = java.time.LocalDate.now(ZoneOffset.UTC).toString();
        String filename = "usuarios_" + today + ".csv";
        return Response.ok(exportUsersUseCase.execute())
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener usuario por ID",
        description = "Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Usuario encontrado")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Response getUser(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathParam("id") @Positive Long id) {
        return userRepository.findById(id)
                .map(u -> Response.ok(u).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(USUARIO_NO_ENCONTRADO).build());
    }

    @POST
    @Operation(summary = "Crear usuario",
        description = "Crea un nuevo usuario en Firebase y en la base de datos. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "409", description = "El correo ya está registrado")
    @APIResponse(responseCode = "500", description = "Error inesperado del servidor")
    @RequestBody(description = "Datos del nuevo usuario", required = true,
        content = @Content(schema = @Schema(implementation = CreateUserDto.class)))
    public Response createUser(@Valid CreateUserDto dto) {
        try {
            return Response.status(Response.Status.CREATED)
                    .entity(createUserUseCase.execute(dto))
                    .build();
        } catch (DuplicateEmailException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("El correo ya está registrado")
                    .build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Unexpected error creating user: {0}", e.getMessage());
            return Response.serverError().entity(ERROR_INESPERADO).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar usuario",
        description = "Actualiza nombre y/o rol de un usuario existente. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Usuario actualizado")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "500", description = "Error inesperado del servidor")
    @RequestBody(description = "Campos a actualizar", required = true,
        content = @Content(schema = @Schema(implementation = UpdateUserDto.class)))
    public Response updateUser(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathParam("id") @Positive Long id,
            @Valid UpdateUserDto dto) {
        try {
            return Response.ok(updateUserUseCase.execute(id, dto)).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(USUARIO_NO_ENCONTRADO)
                    .build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Unexpected error updating user: {0}", e.getMessage());
            return Response.serverError().entity(ERROR_INESPERADO).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar usuario",
        description = "Elimina el usuario. Un ADMIN no puede eliminarse a sí mismo. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "204", description = "Usuario eliminado")
    @APIResponse(responseCode = "400", description = "Operación no permitida (p.ej. auto-eliminación)")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "500", description = "Error inesperado del servidor")
    public Response deleteUser(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathParam("id") @Positive Long id) {
        try {
            deleteUserUseCase.execute(id);
            return Response.noContent().build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(USUARIO_NO_ENCONTRADO)
                    .build();
        } catch (IllegalArgumentException e) {
            log.log(java.util.logging.Level.WARNING, "Invalid argument deleting user: {0}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Operación no permitida")
                    .build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Unexpected error deleting user: {0}", e.getMessage());
            return Response.serverError().entity(ERROR_INESPERADO).build();
        }
    }

    @PATCH
    @Path("/{id}/activate")
    @Operation(summary = "Activar usuario",
        description = "Cambia el estado del usuario a ACTIVE. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Usuario activado")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "409", description = "El usuario ya está activo")
    @APIResponse(responseCode = "500", description = "Error inesperado del servidor")
    public Response activateUser(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathParam("id") @Positive Long id) {
        try {
            return Response.ok(activateUserUseCase.execute(id)).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(USUARIO_NO_ENCONTRADO)
                    .build();
        } catch (UserAlreadyActiveException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("El usuario ya está activo")
                    .build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Unexpected error activating user: {0}", e.getMessage());
            return Response.serverError().entity(ERROR_INESPERADO).build();
        }
    }

    @PATCH
    @Path("/{id}/suspend")
    @Operation(summary = "Suspender usuario",
        description = "Cambia el estado del usuario a SUSPENDED. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Usuario suspendido")
    @APIResponse(responseCode = "400", description = "Operación no permitida")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "409", description = "El usuario ya está suspendido")
    @APIResponse(responseCode = "500", description = "Error inesperado del servidor")
    public Response suspendUser(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathParam("id") @Positive Long id) {
        try {
            return Response.ok(suspendUserUseCase.execute(id)).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(USUARIO_NO_ENCONTRADO)
                    .build();
        } catch (UserAlreadySuspendedException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("El usuario ya está suspendido")
                    .build();
        } catch (IllegalArgumentException e) {
            log.log(java.util.logging.Level.WARNING, "Invalid argument suspending user: {0}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Operación no permitida")
                    .build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Unexpected error suspending user: {0}", e.getMessage());
            return Response.serverError().entity(ERROR_INESPERADO).build();
        }
    }

    @PATCH
    @Path("/{id}/reset-password")
    @Operation(summary = "Restablecer contraseña",
        description = "Permite al ADMIN establecer una nueva contraseña para cualquier usuario sin requerir la contraseña actual. Página: Admin Panel > pestaña Usuarios. **Roles:** ADMIN")
    @APIResponse(responseCode = "204", description = "Contraseña restablecida correctamente")
    @APIResponse(responseCode = "400", description = "Contraseña inválida")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "500", description = "Error inesperado del servidor")
    @RequestBody(description = "Nueva contraseña", required = true,
        content = @Content(schema = @Schema(implementation = ResetPasswordDto.class)))
    public Response resetPassword(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathParam("id") @Positive Long id,
            @Valid ResetPasswordDto dto) {
        try {
            resetPasswordUseCase.execute(id, dto);
            return Response.noContent().build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(USUARIO_NO_ENCONTRADO)
                    .build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Unexpected error resetting password: {0}", e.getMessage());
            return Response.serverError().entity(ERROR_INESPERADO).build();
        }
    }
}
