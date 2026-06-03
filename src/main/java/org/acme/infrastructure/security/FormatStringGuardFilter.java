package org.acme.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rejects requests whose path or query string contain printf-style format
 * specifiers (e.g. %s, %d, %n, %x). Closes the OWASP "Format String Error"
 * attack surface before any downstream parser can crash on malformed input.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
public class FormatStringGuardFilter implements ContainerRequestFilter {

    private static final Pattern FORMAT_SPECIFIER =
            Pattern.compile("%[-+ #0,(]?\\d*(?:\\.\\d+)?[sdifeEgGxXocCbBhH]");

    @Override
    public void filter(ContainerRequestContext ctx) {
        String rawPath = ctx.getUriInfo().getPath(false);
        if (containsFormatSpecifier(rawPath)) {
            ctx.abortWith(badRequest("Path contiene caracteres inválidos"));
            return;
        }

        MultivaluedMap<String, String> query = ctx.getUriInfo().getQueryParameters(false);
        for (Map.Entry<String, List<String>> entry : query.entrySet()) {
            if (containsFormatSpecifier(entry.getKey())) {
                ctx.abortWith(badRequest("Parámetro inválido"));
                return;
            }
            for (String value : entry.getValue()) {
                if (containsFormatSpecifier(value)) {
                    ctx.abortWith(badRequest("Parámetro inválido"));
                    return;
                }
            }
        }
    }

    private static boolean containsFormatSpecifier(String value) {
        if (value == null || value.isEmpty()) return false;
        return FORMAT_SPECIFIER.matcher(value).find();
    }

    private static Response badRequest(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", message);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
