package org.mifos.pheevouchermanagementsystem.util;

public enum VoucherManagementEnum {

    SUCCESS_RESPONSE("00", "Request successfully received by Pay-BB"), FAILED_RESPONSE("01", "Request not acknowledged by Pay-BB");

    private final String value;
    private final String message;

    VoucherManagementEnum(String value, String message) {
        this.value = value;
        this.message = message;
    }

    public String getValue() {
        return this.value;
    }

    public String getMessage() {
        return message;
    }
}
