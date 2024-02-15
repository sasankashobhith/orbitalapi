package com.bundee.msfw.defs;

import com.bundee.msfw.interfaces.logi.BLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BExceptions extends Throwable {
	private static final long serialVersionUID = 1L;

	protected class BException {
		public ProcessingCode code = ProcessingCode.UNKNOWN_PC;
		public String logMsg;
	}

	protected List<BException> exceptions = new ArrayList<BException>();
	private int httpStatus = 200;

	public BExceptions() {
	}

	public BExceptions(ProcessingCode code, String logMsg) {
		add(code, logMsg);
	}

	public BExceptions(Throwable err, ProcessingCode code) {
		add(err, code);
	}

	public boolean hasExceptions() {
		return !exceptions.isEmpty();
	}

	public int getHTTPStatus() {
		return httpStatus;
	}

	public ProcessingCode getCode() {
		ProcessingCode pc = ProcessingCode.UNKNOWN_PC;
		if (!exceptions.isEmpty()) {
			pc = exceptions.get(0).code;
		}
		return pc;
	}

	@Override
	public String getMessage() {
		String msg = "";
		if (!exceptions.isEmpty()) {
			msg = exceptions.get(0).logMsg;
		}
		return msg;
	}

	public void setHTTPStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public void add(ProcessingCode code, String logMsg) {
		if (code == null && logMsg == null)
			return;
		BException bex = new BException();
		bex.code = (code == null ? ProcessingCode.UNKNOWN_PC : code);
		bex.logMsg = (logMsg == null ? "" : logMsg);
		exceptions.add(bex);
	}

	public void add(Throwable err, ProcessingCode code) {
		if (code == null && err == null)
			return;
		String logMsg = "";
		logMsg = extractMessage(err, logMsg);
		add(code, logMsg);
	}

	public void add(BExceptions exs) {
		if (exs != null && !exs.exceptions.isEmpty()) {
			exceptions.addAll(exs.exceptions);
		}
	}

	public void printStackTrace(BLogger logger) {
		if (exceptions != null) {
			Iterator<BException> it = exceptions.iterator();
			while (it.hasNext()) {
				BException bex = it.next();
				logger.error(bex.logMsg);
			}
		}
	}

	private String extractMessage(Throwable ex, String logMsg) {
		if (ex == null || ex.getMessage() == null || ex.getMessage().isBlank())
			return logMsg;
		if (logMsg.contains(ex.getMessage()))
			return "";

		logMsg += (logMsg.isBlank() ? ex.toString() : " :: " + ex.toString());
		return extractMessage(ex.getCause(), logMsg);
	}
}
