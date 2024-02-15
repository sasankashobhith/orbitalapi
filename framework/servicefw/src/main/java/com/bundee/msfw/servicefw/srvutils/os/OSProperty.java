package com.bundee.msfw.servicefw.srvutils.os;

public enum OSProperty {

    OS_PROPERTY("os.components"),
    OS_ACCESSKEY("os.accessKey"),
    OS_SECRETKEY("os.secretKey"),
    OS_ENDPOINT("os.EndPoint"),
    OS_REGION("os.Region"),
    OS_KEYSTOREPATH("os.keyStorePath"),
    OS_KEYSTOREPASSW("os.keyStorePassw"),
    OS_TLSVERSION("os.tlsVersion"),
    OS_SSLCIPHERSUITE("os.sslCipherSuite"),
    OS_INBOXBUCKET("os.inboxBucket"),
    OS_OUTBOXBUCKET("os.outboxBucket"),
    OS_CFG_PFX("os.cfgPfx");

    private final String value;

    OSProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
