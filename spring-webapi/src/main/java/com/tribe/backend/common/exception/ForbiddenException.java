package com.tribe.backend.common.exception;

public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}
