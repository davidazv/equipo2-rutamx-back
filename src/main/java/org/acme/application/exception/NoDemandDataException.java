package org.acme.application.exception;

/**
 * Thrown when a route exists in GTFS but has no afluencia demand data.
 * Used by HU12 to signal that a bus model recommendation cannot be generated.
 */
public class NoDemandDataException extends RuntimeException {

    public NoDemandDataException(String message) {
        super(message);
    }
}
