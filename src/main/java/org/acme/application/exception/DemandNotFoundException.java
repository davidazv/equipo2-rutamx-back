package org.acme.application.exception;

public class DemandNotFoundException extends RuntimeException {
    public DemandNotFoundException(String linea) {
        super("No se encontraron datos de demanda para: " + linea);
    }
}
