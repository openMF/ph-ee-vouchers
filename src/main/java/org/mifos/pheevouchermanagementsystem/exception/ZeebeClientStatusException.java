package org.mifos.pheevouchermanagementsystem.exception;

public class ZeebeClientStatusException extends RuntimeException {

    public ZeebeClientStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}