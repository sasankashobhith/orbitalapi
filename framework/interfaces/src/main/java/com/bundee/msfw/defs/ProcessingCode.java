package com.bundee.msfw.defs;

public class ProcessingCode {
	private int code;
	private String codeKey;

	public static final int SUCCESS = 0;
	public static final int UNKNOWN = -9999;
	public static final String SUCCESS_KEY = "SUCCESS";
	public static final String UNKNOWN_KEY = "UNKNOWN";

	public static final ProcessingCode SUCCESS_PC = new ProcessingCode(SUCCESS, SUCCESS_KEY); 
	public static final ProcessingCode UNKNOWN_PC = new ProcessingCode(UNKNOWN, UNKNOWN_KEY);
	
	public ProcessingCode(int code, String codeKey) {
		this.code = code;
		this.codeKey = codeKey;
	}

	public int getCode() {
		return code;
	}
	public String getCodeKey() {
		return codeKey;
	}
}
