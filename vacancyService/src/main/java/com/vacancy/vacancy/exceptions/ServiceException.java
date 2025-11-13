package com.vacancy.vacancy.exceptions;

public class ServiceException extends RuntimeException {
    
    public ServiceException() {}

    public ServiceException(String message) {
        super(message);
    }
}
