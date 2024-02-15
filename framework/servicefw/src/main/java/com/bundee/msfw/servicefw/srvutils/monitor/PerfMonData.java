package com.bundee.msfw.servicefw.srvutils.monitor;

import com.bundee.msfw.interfaces.logi.BLogger;

public class PerfMonData {
	private String eventID = "unknown";
	private String sessionID = "unknown";
	private MonEvent eventType = MonEvent.UNKNOWN;
	private String eventName = "unknown";
	private Long eventTMS = -1L;
	private Integer procTimeMS = -1;
	private Integer errCode = 0;
	private Integer reqSize = -1;
	private Integer respSize = -1;

	public PerfMonData(String eventID, String sessionID, MonEvent eventType, String eventName) {
		this.eventTMS = System.currentTimeMillis();
		this.eventID = eventID;
		this.sessionID = sessionID;
		this.eventName = eventName;
		this.eventType = eventType;
	}
	
	public void conclude() {
		procTimeMS = Long.valueOf(System.currentTimeMillis() - eventTMS).intValue();
	}
	public String getEventID() {
		return eventID;
	}
	public String getSessionID() {
		return sessionID;
	}
	public MonEvent getEventType() {
		return eventType;
	}
	public String getEventName() {
		return eventName;
	}
	public Long getEventTMS() {
		return eventTMS;
	}
	public Integer getProcTimeMS() {
		return procTimeMS;
	}
	public Integer getErrCode() {
		return errCode;
	}
	public Integer getReqSize() {
		return reqSize;
	}
	public Integer getRespSize() {
		return respSize;
	}


	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
	}
	public void setReqSize(Integer reqSize) {
		this.reqSize = reqSize;
	}
	public void setRespSize(Integer respSize) {
		this.respSize = respSize;
	}
	
	public void log(BLogger logger) {
		//logger.debug("sessionID: [" + sessionID + "] eventID: [" + eventID + "] eventType: [" + eventType.name() + "] eventName: [" + eventName + "] eventTMS: [" + eventTMS + "] procTimeMS: [" + procTimeMS + "] errCode: [" + errCode + "] reqSize: [" + reqSize + "] respSize: [" + respSize + "]");
		logger.debug("sessionID: [" + sessionID + "] eventType: [" + eventType.name() + "] eventName: [" + eventName + "] procTimeMS: [" + procTimeMS + "] errCode: [" + errCode + "] respSize: [" + respSize + "]");
	}
}
