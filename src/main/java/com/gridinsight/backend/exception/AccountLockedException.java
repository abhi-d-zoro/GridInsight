package com.gridinsight.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user account is temporarily locked due to too many failed login attempts.
 * Annotated with @ResponseStatus so Spring MVC automatically returns HTTP 423 without extra handlers.
 */
@ResponseStatus(HttpStatus.LOCKED) // -> returns 423 Locked
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
