package org.mifos.pheevouchermanagementsystem.zeebe.worker;

public enum Worker {
     ACCOUNT_LOOKUP("payee-account-Lookup"), ACCOUNT_LOOKUP_CALLBACK("account-lookup-callback");

    private final String value;

    private Worker(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}
