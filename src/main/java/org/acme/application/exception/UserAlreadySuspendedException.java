package org.acme.application.exception;

public class UserAlreadySuspendedException extends RuntimeException {
    public UserAlreadySuspendedException(String message) {
        super(message);
    }
}
