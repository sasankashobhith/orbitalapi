package com.bundee.msfw.servicefw.fw;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.MultivaluedMap;

import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.PageHandler;
import com.bundee.msfw.interfaces.blmodi.RequestAuditor;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.URLPathProcessor.SearchResult;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;
import com.bundee.msfw.servicefw.srvutils.monitor.PerfMonData;

class ReqRespObjCapsule {
	public BLogger reqLogger;
	public String payLoad;
	public Map<String, String> formHeaders;
	public MultivaluedMap<String, byte[]> formData;
	public Map<String, List<String>> qparameters;
	public Map<String, List<String>> reqHeaders;

	public UTF8String outputJSON;
	public int status = 200;
	public MonitoringTracker perfTracker;
	public PerfMonData perfMonData;
	public DBManager dbm;
	public MultivaluedMap<String, String> respHeaders;
	
	public BLModServices blModServices;
	public Object reqDTOObject;
	public BaseResponse respDTOObject;
	public RequestAuditor reqAuditor;
	public PageHandler pageHandler;
	public Object pageToken;
	public SearchResult sr;

	public void setQParameters(MultivaluedMap<String, String> inQParameters) {
		qparameters = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
		inQParameters.entrySet().stream().filter(e -> !e.getValue().isEmpty()).forEach(entry -> {
			qparameters.put(entry.getKey(), entry.getValue());
		});
	}

	public void setReqHeaders(MultivaluedMap<String, String> inReqHeaders) {
		reqHeaders = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
		inReqHeaders.entrySet().stream().filter(e -> !e.getValue().isEmpty()).forEach(entry -> {
			reqHeaders.put(entry.getKey(), entry.getValue());
		});
	}
}
