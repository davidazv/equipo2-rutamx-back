package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.dto.CreateUserDto;
import org.acme.application.dto.UpdateUserDto;
import org.acme.application.exception.DuplicateEmailException;
import org.acme.application.exception.UserAlreadyActiveException;
import org.acme.application.exception.UserAlreadySuspendedException;
import org.acme.application.exception.UserNotFoundException;
import org.acme.application.usecase.ActivateUserUseCase;
import org.acme.application.usecase.CreateUserUseCase;
import org.acme.application.usecase.DeleteUserUseCase;
import org.acme.application.usecase.SuspendUserUseCase;
import org.acme.application.usecase.UpdateUserUseCase;
import org.acme.domain.repository.UserRepository;

import java.util.logging.Logger;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger log = Logger.getLogger(UserResource.class.getName());

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final SuspendUserUseCase suspendUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;

    // Exception to the no-direct-repo-in-resource rule: read-only list/get-one
    // do not justify a separate use case (per HU01-04 spec).
    private final UserRepository userRepository;

    @Inject
    public UserResource(CreateUserUseCase createUserUseCase,
                        UpdateUserUseCase updateUserUseCase,
                        DeleteUserUseCase deleteUserUseCase,
                        SuspendUserUseCase suspendUserUseCase,
                        ActivateUserUseCase activateUserUseCase,
                        UserRepository userRepository) {
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.suspendUserUseCase = suspendUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.userRepository = userRepository;
    }

    @GET
    public Response listUsers() {
        return Response.ok(userRepository.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") Long id) {
        return userRepository.findById(id)
                .map(u -> Response.ok(u).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Usuario no encontrado").build());
    }

    @POST
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
            log.severe("Unexpected error creating user: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, UpdateUserDto dto) {
        try {
            return Response.ok(updateUserUseCase.execute(id, dto)).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Usuario no encontrado")
                    .build();
        } catch (Exception e) {
            log.severe("Unexpected error updating user " + id + ": " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            deleteUserUseCase.execute(id);
            return Response.noContent().build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Usuario no encontrado")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.severe("Unexpected error deleting user " + id + ": " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @PATCH
    @Path("/{id}/activate")
    public Response activateUser(@PathParam("id") Long id) {
        try {
            return Response.ok(activateUserUseCase.execute(id)).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Usuario no encontrado")
                    .build();
        } catch (UserAlreadyActiveException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("El usuario ya está activo")
                    .build();
        } catch (Exception e) {
            log.severe("Unexpected error activating user " + id + ": " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @PATCH
    @Path("/{id}/suspend")
    public Response suspendUser(@PathParam("id") Long id) {
        try {
            return Response.ok(suspendUserUseCase.execute(id)).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Usuario no encontrado")
                    .build();
        } catch (UserAlreadySuspendedException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("El usuario ya está suspendido")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.severe("Unexpected error suspending user " + id + ": " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
