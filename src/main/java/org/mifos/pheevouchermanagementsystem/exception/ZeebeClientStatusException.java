package org.mifos.pheevouchermanagementsystem.exception;

public class ZeebeClientStatusException extends RuntimeException {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZeebeClientStatusException(String message, String id) {
        super(message);
        this.id = id;
    }
}
