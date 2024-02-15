package com.bundee.msfw.interfaces.restclienti;

import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface RESTClient extends HealthCheck {
	String getClientID();
	void setStandardHealthCheck(String hostPort);
	
	ResponseCapsule sendBytes(BLogger logger, String url, Map<String, String> reqHeaders, String mimeType, byte[] data, Class<?> respClass, Map<String, String> respHeaders) throws BExceptions;
	ResponseCapsule receiveBytes(BLogger logger, String method, String url, Map<String, String> reqHeaders, Object reqObj, Map<String, String> respHeaders) throws BExceptions;
	ResponseCapsule sendReceiveJSONData(BLogger logger, String method, String url, Map<String, String> headers, Object reqObj, Class<?> respClass, Map<String, String> respHeaders) throws BExceptions;
	
	Object sendReceive(BLogger logger, String method, String url, Map<String, String> headers, Object reqObj, Class<?> respClass, Map<String, String> respHeaders) throws BExceptions;
}
