package org.mifos.pheevouchermanagementsystem.util;

public enum RedemptionStatusEnum {
    FAILURE("00"),
    SUCCESS("01");
    private final String value;
    RedemptionStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
