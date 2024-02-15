package com.bundee.msfw.servicefw.srvutils.utils;

import java.util.List;
import java.util.Map;

import com.bundee.msfw.interfaces.fcfgi.Application;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;

public class ThreadLocalData {
	private static final String DEF_CALLER_ID = "no-caller-req-id";
	String callerReqID = DEF_CALLER_ID;
	MonitoringTracker monTracker = null;

	public void extractRequiredData(BLogger logger, Map<String, List<String>> reqHeaders) {
		if(reqHeaders != null && !reqHeaders.isEmpty()) {
			if(reqHeaders.containsKey(FwConstants.GENERAL_CONSTANTS.CALLER_REQ_HEADER)) {
				List<String> vals = reqHeaders.get(FwConstants.GENERAL_CONSTANTS.CALLER_REQ_HEADER);
				if(vals != null && !vals.isEmpty()) {
					callerReqID = vals.get(0);
					logger.debug("extractRequiredData: CallerReqID: " + callerReqID + " ThreadID: " + Thread.currentThread().getId());
				}
			}
		}
	}
	
	public void setRequiredDataInHeaders(Map<String, String> headers) {
		headers.put(FwConstants.GENERAL_CONSTANTS.CALLER_REQ_HEADER, callerReqID);
	}
	
	public void setMonitoringTracker(MonitoringTracker monTracker) {
		this.monTracker = monTracker;
	}
	
	public void setCallerReqID(BLogger logger, Application thisApp, String reqID) {
		if(callerReqID.equalsIgnoreCase(DEF_CALLER_ID)) {
			callerReqID = thisApp.getName() + "_" + thisApp.getThisHost() + "_" + reqID;
			logger.debug("setCallerReqID: CallerReqID: " + callerReqID + " ThreadID: " + Thread.currentThread().getId());
		}
	}
	public String getCallerReqID() {
		return callerReqID;
	}
	
	public MonitoringTracker getMonitoringTracker() {
		return monTracker;
	}
	
	public void copy(ThreadLocalData tld) {
		if(tld == null) return;
		
		this.callerReqID = tld.callerReqID;
		this.monTracker = tld.monTracker;
	}
}
