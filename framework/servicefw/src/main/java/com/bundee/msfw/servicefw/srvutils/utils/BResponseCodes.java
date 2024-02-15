package com.bundee.msfw.servicefw.srvutils.utils;

import java.util.ArrayList;
import java.util.List;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.ProcessingCode;

public class BResponseCodes extends BExceptions {
	private static final long serialVersionUID = 1L;

	class RespCode {
		public int code;
		public String key;
		public String message;
		
		RespCode(BException ex) {
			code = ex.code.getCode();
			key = ex.code.getCodeKey();
			message = ex.logMsg;
		}
	}

	public BResponseCodes() {
	}

	public BResponseCodes(ProcessingCode code, String logMsg) {
		super(code, logMsg);
	}

	public List<RespCode> getCodes() {
		List<RespCode> codes = new ArrayList<RespCode>();
		exceptions.forEach(ex -> {
			codes.add(new RespCode(ex));
		});
		return codes;
	}
}
