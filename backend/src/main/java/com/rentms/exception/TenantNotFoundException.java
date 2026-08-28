package com.rentms.exception;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }
}