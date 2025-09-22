package com.tribe.backend.common.exception;

public class ServiceUnavailableException extends DomainException {

    public ServiceUnavailableException(String message) {
        super("SERVICE_UNAVAILABLE", message);
    }
}
