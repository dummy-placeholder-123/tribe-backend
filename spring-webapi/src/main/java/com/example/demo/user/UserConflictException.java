package com.example.demo.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class UserConflictException extends RuntimeException {

    UserConflictException(String message) {
        super(message);
    }
}
