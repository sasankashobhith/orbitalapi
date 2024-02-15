package com.bundee.msfw.servicefw.srvutils.monitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.restclienti.ResponseCapsule;
import com.bundee.msfw.servicefw.srvutils.restclient.RESTClientImpl;
import com.bundee.msfw.servicefw.srvutils.utils.TLSDataCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.ThreadLocalData;

public class RESTClientMonitor implements RESTClient {

	RESTClientImpl restClient;
	
	public RESTClientMonitor(RESTClientImpl restClient) {
		this.restClient = restClient;
	}
	
	public SSLConnectionSocketFactory getSSLFactory() throws BExceptions {
		return restClient.getSSLFactory();
	}
	
	@Override
	public void setStandardHealthCheck(String stdHostPort) {
		restClient.setStandardHealthCheck(stdHostPort);
	}
	
	@Override
	public Object sendReceive(BLogger logger, String method, String url, Map<String, String> headers, Object reqObj,
			Class<?> respClass, Map<String, String> respHeaders) throws BExceptions {
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        MonitoringTracker perfTracker = tld.getMonitoringTracker();
		PerfMonData perfData = (perfTracker == null ? null : perfTracker.startTracking(MonEvent.REST_CLIENT, getHost(url)));
		
		Object val = null;
		try {
			val = restClient.sendReceive(logger, method, url, headers, reqObj, respClass, respHeaders);
			if(perfTracker != null) perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			if(perfTracker != null) perfTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public ResponseCapsule sendReceiveJSONData(BLogger logger, String method, String url, Map<String, String> headers, Object reqObj,
			Class<?> respClass, Map<String, String> respHeaders) throws BExceptions {
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        MonitoringTracker perfTracker = tld.getMonitoringTracker();
		PerfMonData perfData = (perfTracker == null ? null : perfTracker.startTracking(MonEvent.REST_CLIENT, getHost(url)));
		
		ResponseCapsule val = null;
		try {
			val = restClient.sendReceiveJSONData(logger, method, url, headers, reqObj, respClass, respHeaders);
			if(perfTracker != null) {
				perfTracker.endTracking(perfData);
				if(val != null) {
					perfData.setRespSize(val.getRespSize());
				}
			}
		} catch (BExceptions ex) {
			if(perfTracker != null) {
				perfTracker.endTracking(perfData);
			}
			throw ex;
		}
		return val;
	}
	
	@Override
	public ResponseCapsule sendBytes(BLogger logger, String url, Map<String, String> headers, String fileName,
			byte[] data, Class<?> respClass, Map<String, String> respHeaders) throws BExceptions {
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        MonitoringTracker perfTracker = tld.getMonitoringTracker();
		PerfMonData perfData = (perfTracker == null ? null : perfTracker.startTracking(MonEvent.REST_CLIENT, getHost(url)));
		
		ResponseCapsule rc = null;
		try {
			rc = restClient.sendBytes(logger, url, headers, fileName, data, respClass, respHeaders);
			if(perfTracker != null) {
				perfTracker.endTracking(perfData);
				if(rc != null) {
					perfData.setRespSize(rc.getRespSize());
				}
			}
		} catch (BExceptions ex) {
			if(perfTracker != null) perfTracker.endTracking(perfData);
			throw ex;
		}
		return rc;
	}


	@Override
	public ResponseCapsule receiveBytes(BLogger logger, String method, String url, Map<String, String> reqHeaders,
			Object reqObj, Map<String, String> respHeaders) throws BExceptions {
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        MonitoringTracker perfTracker = tld.getMonitoringTracker();
		PerfMonData perfData = (perfTracker == null ? null : perfTracker.startTracking(MonEvent.REST_CLIENT, getHost(url)));
		
		ResponseCapsule rc = null;
		try {
			rc = restClient.receiveBytes(logger, method, url, reqHeaders, reqObj, respHeaders);
			if(perfTracker != null) {
				perfTracker.endTracking(perfData);
				if(rc != null) {
					perfData.setRespSize(rc.getRespSize());
				}
			}
		} catch (BExceptions ex) {
			if(perfTracker != null) perfTracker.endTracking(perfData);
			throw ex;
		}
		return rc;
	}
	
	private String getHost(String url) {
		return url;
		/*
		String urlHost = "unknown";
		try {
			URL urlo = new URL(url);
			urlHost = urlo.getHost() + ":" + urlo.getPort();
		} catch (MalformedURLException e) {
		}
		return urlHost;
		*/
	}
	
	@Override
	public HealthDetails checkHealth(BLogger logger) {
		return restClient.checkHealth(logger);
	}

	@Override
	public String getClientID() {
		return restClient.getClientID();
	}
}
