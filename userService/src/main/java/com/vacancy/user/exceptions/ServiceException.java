package com.vacancy.user.exceptions;

public class ServiceException extends RuntimeException {
    
    public ServiceException() {}

    public ServiceException(String message) {
        super(message);
    }
}
