package com.bundee.msfw.servicefw.srvutils.monitor;

import com.bundee.msfw.interfaces.logi.BLogger;

public class SvcMonData {
	private String eventID = "unknown";
	private MonEvent serviceType = MonEvent.UNKNOWN;
	private String serviceName = "unknown";
	private Long eventTMS = -1L;
	private String extHost = "unknown";
	private Integer errCode = 0;
	private String errDetail = "";

	public SvcMonData(String eventID, MonEvent serviceType, String serviceName) {
		this.eventTMS = System.currentTimeMillis();
		this.eventID = eventID;
		this.serviceName = serviceName;
		this.serviceType = serviceType;
	}
	
	public String getEventID() {
		return eventID;
	}
	public MonEvent getServiceType() {
		return serviceType;
	}
	public String getServiceName() {
		return serviceName;
	}
	public Long getEventTMS() {
		return eventTMS;
	}
	public Integer getErrCode() {
		return errCode;
	}
	public String getErrDetail() {
		return errDetail;
	}
	public String getExtHost() {
		return extHost;
	}

	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
	}
	public void setErrDetail(String errDetail) {
		if(errDetail != null && !errDetail.isBlank()) {
			this.errDetail = errDetail;
		}
	}
	public void setExtHost(String extHost) {
		if(extHost != null && !extHost.isBlank()) {
			this.extHost = extHost;
		}
	}
	
	public void log(BLogger logger) {
		logger.trace("eventID: [" + eventID + "] serviceType: [" + serviceType.name() + "] serviceName: [" + serviceName + "] eventTMS: [" + eventTMS + "] extHost: [" + extHost + "] errCode: [" + errCode + "] errDetail: [" + errDetail + "]");
	}
}
