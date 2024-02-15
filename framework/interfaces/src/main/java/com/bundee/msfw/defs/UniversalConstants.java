package com.bundee.msfw.defs;

public class UniversalConstants {
    public static final int GLOBAL_TENANT_ID = 0;
    public static final String SYSTEM_DEFAULT_LOCALE = "en_US";
    public static final String ERROR_MESSAGE_ILLEGAL_ACCESS = "Error while accessing field";
    public static final String ERROR_MESSAGE_MAX_STRING_LENGTH = " : String length is more than ";
    public static final String ERROR_MESSAGE_MANDATORY_FIELD = " %s field validation failed ";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String SPECIAL_NO_VALIDATE_PERMISSION = "permission_dont_validate";
    public static final String AUTH_TOKEN_HEADER = "bundee_auth_token";
    public static final String HOST_HEADER = "bundee_host";

    public static class PCodes {
        public static final ProcessingCode INTERNAL_ERROR = new ProcessingCode(500, "INTERNAL_ERROR");
        public static final ProcessingCode ACCESS_DENIED = new ProcessingCode(501, "ACCESS_DENIED");
    }
}
