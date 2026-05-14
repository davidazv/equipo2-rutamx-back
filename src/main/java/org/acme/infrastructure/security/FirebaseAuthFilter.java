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
import com.google.firebase.auth.FirebaseToken;
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

        // Endpoints restricted to authenticated roles (CMO, CEO, COO, ADMIN)
        boolean requiresRoleAuth = path.startsWith("/api/co2-savings")
                || path.startsWith("/api/routes/trips-by-day");

        if (requiresRoleAuth) {
            User user = authenticateRequest(requestContext);
            if (user == null) return;

            String role = user.getRoleName() != null ? user.getRoleName().toUpperCase() : "";
            boolean isAllowed = UserStatus.ACTIVE.equals(user.getStatus())
                    && (role.equals("ADMIN") || role.equals("CMO")
                        || role.equals("CEO") || role.equals("COO"));
            if (!isAllowed) {
                requestContext.abortWith(
                        Response.status(Response.Status.FORBIDDEN)
                                .entity("Acceso denegado: se requiere rol autorizado")
                                .build());
                return;
            }
            authContext.setUser(user);
            return;
        }

        // Skip auth for Quarkus internals and public API endpoints
        if (path.startsWith("/q/")
                || path.startsWith("/status")
                || path.startsWith("/api/agencies")
                || path.startsWith("/api/bus-models")
                || path.startsWith("/api/routes")
                || path.startsWith("/api/roi")
                || path.startsWith("/api/kpi")
                || path.startsWith("/api/energy-consumption")
                || path.startsWith("/api/fuel-savings")
                || path.startsWith("/admin/users")
                || path.startsWith("/admin/upload")) {
            return;
        }

        User user = authenticateRequest(requestContext);
        if (user == null) return;

        if (!UserStatus.ACTIVE.equals(user.getStatus()) || !"ADMIN".equals(user.getRoleName())) {
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("Acceso denegado")
                            .build());
            return;
        }

        authContext.setUser(user);
    }

    private User authenticateRequest(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token de autorización requerido")
                            .build());
            return null;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        FirebaseToken firebaseToken;
        try {
            firebaseToken = firebaseUserCreator.verifyIdTokenFull(token);
        } catch (Exception e) {
            log.warning("Invalid Firebase token: " + e.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token inválido o expirado")
                            .build());
            return null;
        }

        String firebaseUuid = firebaseToken.getUid();
        User user = userRepository.findByFirebaseUuid(firebaseUuid).orElse(null);

        // Fallback: some users were created without a firebase_uuid set.
        // If the UUID lookup fails, try matching by email from the verified token
        // and auto-repair the stored UUID so future lookups succeed.
        if (user == null) {
            String email = firebaseToken.getEmail();
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    log.info("Auto-repairing firebase_uuid for user: " + email);
                    user.setFirebaseUuid(firebaseUuid);
                    userRepository.update(user);
                }
            }
        }

        if (user == null) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Usuario no encontrado")
                            .build());
            return null;
        }

        return user;
    }
}
