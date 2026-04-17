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

    @Override
    public void filter(ContainerRequestContext ctx) {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@rutamx.com");
        testUser.setFirebaseUuid("seed-admin-placeholder");
        testUser.setRoleName("ADMIN");
        testUser.setStatus(UserStatus.ACTIVE);
        authContext.setUser(testUser);
    }
}
