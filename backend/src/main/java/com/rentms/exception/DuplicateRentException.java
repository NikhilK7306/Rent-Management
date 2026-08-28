package com.rentms.exception;

public class DuplicateRentException extends RuntimeException {

    public DuplicateRentException(String message) {
        super(message);
    }
}