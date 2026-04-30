package org.acme.application.exception;

public class BusModelAlreadyExistsException extends RuntimeException {
    public BusModelAlreadyExistsException(String name) {
        super("Ya existe un modelo de bus con el nombre: " + name);
    }
}
