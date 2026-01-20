package com.vacancy.vacancy.application.exception;

/**
 * Base Domain Exception - все исключения бизнес-логики наследуются от этого
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
    
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
