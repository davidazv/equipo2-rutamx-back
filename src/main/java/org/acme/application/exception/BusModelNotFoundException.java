package org.acme.application.exception;

public class BusModelNotFoundException extends RuntimeException {
    public BusModelNotFoundException(Long id) {
        super("Modelo de bus no encontrado: " + id);
    }
}
