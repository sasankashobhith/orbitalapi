	package com.bundee.msfw.servicefw.srvutils.os;

public class OSProperties {
    private String accessKey;
    private String secretKey;
    private String s3EndPoint;
    private String s3Region;
    private String inboxBucketName;
    private String outboxBucketName;
    private String cfgPfx;
    
    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getS3EndPoint() {
        return s3EndPoint;
    }

    public void setS3EndPoint(String s3EndPoint) {
        this.s3EndPoint = s3EndPoint;
    }

    public String getS3Region() {
        return s3Region;
    }

    public void setS3Region(String s3Region) {
        this.s3Region = s3Region;
    }

    public String getCfgPfx() {
        return cfgPfx;
    }

    public void setCfgPfx(String cfgPfx) {
        this.cfgPfx = cfgPfx;
    }
    
    public String getInboxBucketName() {
        return inboxBucketName;
    }

    public void setInboxBucketName(String inboxBucketName) {
        this.inboxBucketName = inboxBucketName;
    }

    public String getOutboxBucketName() {
        return outboxBucketName;
    }

    public void setOutboxBucketName(String outboxBucketName) {
        this.outboxBucketName = outboxBucketName;
    }
}
