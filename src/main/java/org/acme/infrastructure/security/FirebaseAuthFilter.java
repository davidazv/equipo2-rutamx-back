package org.acme.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;

import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class FirebaseAuthFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(FirebaseAuthFilter.class.getName());

    @Inject
    FirebaseUserCreator firebaseUserCreator;

    @Inject
    UserRepository userRepository;

    @Inject
    AuthContext authContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("/q/")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token de autorización requerido")
                            .build());
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        String firebaseUuid;
        try {
            firebaseUuid = firebaseUserCreator.verifyIdToken(token);
        } catch (Exception e) {
            log.warning("Invalid Firebase token: " + e.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token inválido o expirado")
                            .build());
            return;
        }

        User user = userRepository.findByFirebaseUuid(firebaseUuid).orElse(null);
        if (user == null) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Usuario no encontrado")
                            .build());
            return;
        }

        if (!UserStatus.ACTIVE.equals(user.getStatus()) || !"ADMIN".equals(user.getRoleName())) {
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("Acceso denegado")
                            .build());
            return;
        }

        authContext.setUser(user);
    }
}
