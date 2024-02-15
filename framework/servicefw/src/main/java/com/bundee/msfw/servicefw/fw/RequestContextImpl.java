package com.bundee.msfw.servicefw.fw;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bundee.msfw.interfaces.blmodi.TokenDetails;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.reqrespi.RequestContext;

public class RequestContextImpl implements RequestContext {
	private static RequestContextImpl rci = new RequestContextImpl();
	private static TokenDetailsDummy tdDummy = rci.new TokenDetailsDummy();
	
	String callerIP = "not_found";
	String uri = "";
	String locale = "";
	long requestTS = 0;
	String reqID = "";
	TokenDetails td = tdDummy;
	
	Map<String, HttpCookie> inCookies = new TreeMap<String, HttpCookie>(String.CASE_INSENSITIVE_ORDER);
	Map<String, HttpCookie> outCookies = new TreeMap<String, HttpCookie>(String.CASE_INSENSITIVE_ORDER);
	Map<String, String> outHeaders = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	Map<String, List<String>> inHeaders = null;
	
	public RequestContextImpl() {
	}
	
	@Override
	public String getCallerIP() {
		return callerIP;
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public String getLocale() {
		return locale;
	}

	@Override
	public long getRequestTSMillis() {
		return requestTS;
	}

	@Override
	public String getRequestID() {
		return reqID;
	}
	
	@Override
	public String toString() {
		return ("[" + getNoNullValue(callerIP) + "][" + getNoNullValue(uri) + "][" + getNoNullValue(locale) + "][" + getNoNullValue(reqID) + "]");
	}
	
	public void log(BLogger logger) {
		logger.info(toString());
	}

	public void setCallerIP(String callerIP) {
		if(callerIP != null && !callerIP.isBlank()) {
			this.callerIP = callerIP;
		}
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setRequestTS(long requestTS) {
		this.requestTS = requestTS;
	}

	public void setRequestID(String reqID) {
		this.reqID = reqID;
	}

	private static String getNoNullValue(String val) {
		if(val == null) return "NA";
		return val;	
	}

	@Override
	public void setOutCookie(String name, HttpCookie cookie) {
		outCookies.put(name, cookie);
	}
	
	@Override
	public void setOutHeader(String name, String value) {
		outHeaders.put(name, value);
	}
	
	public Map<String, String> getAllOutHeaders() {
		return outHeaders;
	}
	public Map<String, HttpCookie> getAllOutCookies() {
		return outCookies;
	}

	@Override
	public HttpCookie getInCookie(String name) {
		if(inCookies.containsKey(name)) {
			return inCookies.get(name);
		}
		return null;
	}

	public void setInCookie(String name, HttpCookie value) {
		inCookies.put(name, value);
	}
	
	public void setInHeaders(Map<String, List<String>> inHeaders) {
		this.inHeaders = inHeaders;
	}
	
	public void setTokenDetails(TokenDetails td) {
		this.td = td;
	}
	
	public Map<String, List<String>> getInHeaders() {
		return inHeaders;
	}

	@Override
	public TokenDetails getTokenDetails() {
		return td;
	}
	
	public boolean isLocalHost() {
		return (callerIP != null ? callerIP.equalsIgnoreCase("localhost") || callerIP.equalsIgnoreCase("127.0.0.1") : false); 
	}
	
	private class TokenDetailsDummy implements TokenDetails {
		@Override
		public Boolean isUserToken() {
			return false;
		}

		@Override
		public Integer getUserID() {
			return 0;
		}
	}
}
