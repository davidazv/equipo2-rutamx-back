package org.acme.application.exception;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(String routeId) {
        super("Ruta no encontrada: " + routeId);
    }
}
