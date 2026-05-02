package org.acme.application.exception;

public class NoGtfsDataException extends RuntimeException {

    public NoGtfsDataException() {
        super("No hay datos GTFS cargados en la base de datos");
    }
}
