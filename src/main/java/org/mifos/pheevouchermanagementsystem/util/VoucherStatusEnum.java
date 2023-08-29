package org.mifos.pheevouchermanagementsystem.util;

public enum VoucherStatusEnum {
    INACTIVE("01"),
    ACTIVE("02"),
    CANCELLED("03"),
    EXPIRED("04"),
    UTILIZED("05"),
    SUSPENDED("06"),
    ERROR("07");
    private final String value;
    VoucherStatusEnum(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        String name = name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    public String getValue() {
        return this.value;
    }
}
