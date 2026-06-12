package org.acme.interfaces.rest;

import io.quarkus.test.Mock;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.Provider;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.infrastructure.security.AuthContext;
import org.acme.infrastructure.security.FirebaseAuthFilter;

@Mock
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class TestFirebaseAuthFilter extends FirebaseAuthFilter {

    @Inject
    AuthContext authContext;

    public TestFirebaseAuthFilter() {
        super(null, null, null);
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getPath();
        String role = path.startsWith("/api/reports") ? "CMO" : "ADMIN";

        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@rutamx.com");
        testUser.setFirebaseUuid("seed-admin-placeholder");
        testUser.setRoleName(role);
        testUser.setStatus(UserStatus.ACTIVE);
        authContext.setUser(testUser);
    }
}
