package com.tribe.backend.common.exception;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
