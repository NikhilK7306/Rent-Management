package com.rentms.exception;

public class RentNotFoundException extends RuntimeException {

    public RentNotFoundException(String message) {
        super(message);
    }
}