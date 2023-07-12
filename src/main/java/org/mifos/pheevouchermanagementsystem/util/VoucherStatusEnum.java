package org.mifos.pheevouchermanagementsystem.util;

public enum VoucherStatusEnum {
    INACTIVE("01"),
    ACTIVE("02"),
    CANCELLED("03"),
    EXPIRED("04");
    private final String value;
    VoucherStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
