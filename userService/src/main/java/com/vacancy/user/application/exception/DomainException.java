package com.vacancy.user.application.exception;

/**
 * Base Domain Exception
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
