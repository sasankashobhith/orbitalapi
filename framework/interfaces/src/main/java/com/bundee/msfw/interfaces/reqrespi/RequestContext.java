package com.bundee.msfw.interfaces.reqrespi;

import java.net.HttpCookie;
import java.util.*;

import com.bundee.msfw.interfaces.blmodi.TokenDetails;

public interface RequestContext {
	String getCallerIP();
	String getURI();
	String getLocale();
	long getRequestTSMillis();
	String getRequestID();
	TokenDetails getTokenDetails();
	public Map<String, List<String>> getInHeaders();
	HttpCookie getInCookie(String name);
	void setOutCookie(String name, HttpCookie cookie);
	void setOutHeader(String name, String value);
}
