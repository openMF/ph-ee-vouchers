package org.mifos.pheevouchermanagementsystem.zeebe;

public final class ZeebeVariables {

    private ZeebeVariables() {}

    public static final String ACCOUNT_LOOKUP_FAILED = "accountLookupFailed";
    public static final String ORIGIN_CHANNEL_REQUEST = "originChannelRequest";
    public static final String CALLBACK = "X-CallbackURL";
    public static final String HOST = "externalApiCallHost";
    public static final String CACHED_TRANSACTION_ID = "cachedTransactionId";
    public static final String TENANT_ID = "tenantId";
    public static final String INITIATOR_FSP_ID = "initiatorFspId";
    public static final String REGISTERING_INSTITUTION_ID = "X-Registering-Institution-ID";
    public static final String PAYER_IDENTIFIER = "payerIdentifier";
    public static final String PAYER_IDENTIFIER_TYPE = "payerIdentifierType";
    public static final String REQUEST_ID = "requestId";
    public static final String PAYEE_IDENTITY = "payeeIdentity";
    public static final String PAYMENT_MODALITY = "paymentModality";
    public static final String MAX_RETRY = "maxRetry";
    public static final String RETRY = "retry";
    public static final String THRESHOLD_DELAY = "thresholdDelay";
    public static final String TRANSACTION_COMPLETED = "transactionCompleted";
    public static final String PARTY_LOOKUP_FSP_ID = "partyLookupFspId";

}
