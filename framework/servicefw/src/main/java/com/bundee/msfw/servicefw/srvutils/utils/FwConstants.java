package com.bundee.msfw.servicefw.srvutils.utils;

import com.bundee.msfw.defs.ProcessingCode;

public class FwConstants {
    private static ProcessingCode newPC(int code, String codeKey) {
        ProcessingCode pc = new ProcessingCode(code, codeKey);
        return pc;
    }

    public static class PCodes {
        public static final ProcessingCode SERVICE_UNAVAILABLE = newPC(1, "SERVICE_UNAVAILABLE");
        public static final ProcessingCode INTERNAL_ERROR = newPC(2, "INTERNAL_ERROR");
        public static final ProcessingCode CONFIGURATION_MISSING = newPC(3, "CONFIGURATION_MISSING");

        public static final ProcessingCode REQUEST_NOT_SUPPORTED = newPC(50, "REQUEST_NOT_SUPPORTED");

        public static final ProcessingCode INVALID_VALUE = newPC(100, "INVALID_VALUE");
        public static final ProcessingCode MANDATORY_FIELD_MISSING = newPC(101, "MANDATORY_FIELD_MISSING");

        public static final ProcessingCode REST_CONN_ERROR = newPC(150, "REST_CONN_ERROR");
        public static final ProcessingCode SESSION_INVALID = newPC(151, "SESSION_INVALID");
   
        //Service failures
        public static final ProcessingCode VAULT_FAILURE = newPC(200, "VAULT_FAILURE");

        public static final ProcessingCode OBJECT_STORE_SERVICE_FAILURE = newPC(202, "OBJECT_STORE_SERVICE_FAILURE");

        //Emailer Service failure
		public static final ProcessingCode EMAILER_SERVICE_FAILURE =  newPC(206, "EMAILER_SERVICE_FAILURE");
    }

	public static class GENERAL_CONSTANTS {
		public static final String CALLER_REQ_HEADER = "bundee_caller-req-id";
		public static final String PAGE_TOKEN = "bundee-api-pg-token";
	}
	
	public static class ENDPOINT_URLS {
		public static final String SVC_HEALTH_DETAILS = "api/v1/service/details/health";
	}
	
	public static class ENDPOINT_PERMISSIONS {
		public static final String SVC_HEALTH_DETAILS = "SVC_HEALTH_DETAILS";
	}
}
