package com.bundee.ums.defs;

import com.bundee.msfw.defs.ProcessingCode;

public class UMSProcessingCode extends ProcessingCode {

	public UMSProcessingCode(int code, String codeKey) {
		super(code, codeKey);
	}

	
	public static final UMSProcessingCode DUPLICATE_USERS = new UMSProcessingCode(1001, "DUPLICATE_USERS"); 
	public static final UMSProcessingCode USERS_NOT_FOUND = new UMSProcessingCode(1002, "USERS_NOT_FOUND");
	public static final UMSProcessingCode INVALID_USER = new UMSProcessingCode(1003, "INVALID_USER_DETAILS");
	public static final UMSProcessingCode INVALID_EMAIL = new UMSProcessingCode(1004, "INVALID_USER_DETAILS");
	public static final UMSProcessingCode INVALID_CONFIGURATION = new UMSProcessingCode(1005, "INVALID_CONFIGURATION");
	public static final UMSProcessingCode INVALID_AUTH_TOKEN = new UMSProcessingCode(1006, "INVALID_AUTH_TOKEN");
	public static final UMSProcessingCode FB_ERROR = new UMSProcessingCode(1007, "FB_ERROR");
	public static final UMSProcessingCode ACCESS_DENIED = new UMSProcessingCode(1008, "ACCESS_DENIED");
	public static final UMSProcessingCode INVALID_INPUT = new UMSProcessingCode(1009, "INVALID_INPUT");
}
