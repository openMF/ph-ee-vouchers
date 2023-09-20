package org.mifos.pheevouchermanagementsystem.zeebe.worker;

public enum Worker {

    ACCOUNT_LOOKUP("payee-account-Lookup"), ACCOUNT_LOOKUP_CALLBACK("account-lookup-callback"), BATCH_AUTH(
            "call-batch-authorization"), CREATE_VOUCHERS("create-vouchers");

    private final String value;

    Worker(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}
