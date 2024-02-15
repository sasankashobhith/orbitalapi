package com.bundee.msfw.servicefw.srvutils.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.srvutils.utils.BJson;
import com.bundee.msfw.servicefw.srvutils.utils.TLSDataCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.ThreadLocalData;

public class RESTHelper {
	private static final String CT_APPLICTION_JSON = "application/json";
	private static final String CT_TYPE = "Content-Type";
	private static final Set<Integer> validStatusCodes = new HashSet<Integer>();
	
	static {
		validStatusCodes.add(HttpStatus.SC_OK);
		validStatusCodes.add(HttpStatus.SC_CREATED);
	}
	
	private CloseableHttpClient httpClient;
	private byte[] responseBytes = null;
	private String url;
	private Map<String, String> respHeaders;

	public RESTHelper(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
		respHeaders = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	}

	public void sendJSONData(BLogger logger, String method, String url, Map<String, String> reqHeaders,
			Object reqObj) throws BExceptions {
		CloseableHttpResponse response = null;
		try {
			this.url = url;
			String reqJSON = null;
			if (reqObj != null) {
				BJson gson = new BJson();
				UTF8String str = gson.toJson(reqObj);
				if(str != null) {
					reqJSON = str.getUTF8String();
				}
			}

			ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
			if (reqHeaders == null) {
				reqHeaders = new TreeMap<String, String>();
			}
			tld.setRequiredDataInHeaders(reqHeaders);

			HttpRequest request = createHttpRequest(logger, method, url, reqJSON);
			if (reqHeaders != null && !reqHeaders.isEmpty()) {
				for (Map.Entry<String, String> header : reqHeaders.entrySet()) {
					request.setHeader(header.getKey(), header.getValue());
				}
			}
			request.setHeader(HttpHeaders.CONTENT_TYPE, CT_APPLICTION_JSON);

			logger.info("sendJSONData: sending request to " + url);
			response = httpClient.execute((HttpUriRequest) request);
			logger.info("sendJSONData: response received " + url);
			collectResponseBytes(logger, response);
			logger.info("sendJSONData: data collected from response " + url);
		} catch (IOException e) {
			logger.error(e);
			throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, url + " " + e.getMessage());
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, url + " " + e.getMessage());
				}
			}
		}
	}

	public void sendBytesData(BLogger logger, String url, Map<String, String> reqHeaders, String mimeType,
			byte[] data) throws BExceptions {
		this.url = url;
		CloseableHttpResponse response = null;
		try {
			HttpPost post = new HttpPost(url);

			EntityBuilder entityBuilder = EntityBuilder.create();
			entityBuilder.setBinary(data);

			HttpEntity entity = entityBuilder.build();
			post.setEntity(entity);
			if (reqHeaders != null && !reqHeaders.isEmpty()) {
				for (Map.Entry<String, String> pair : reqHeaders.entrySet()) {
					post.setHeader(pair.getKey(), pair.getValue());
				}
			}

			if (mimeType != null && !mimeType.isBlank()) {
				post.setHeader(CT_TYPE, mimeType);
				logger.debug("setting Content-Type: " + mimeType);
			}

			logger.info("sendBytesData: sending request to " + url);
			response = httpClient.execute(post);
			logger.info("sendBytesData: response received " + url);
			collectResponseBytes(logger, response);
			logger.info("sendBytesData: data collected from response " + url);			
		} catch (IOException e) {
			logger.error(e);
			throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, url + " " + e.getMessage());
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, url + " " + e.getMessage());
				}
			}
		}
	}

	private void collectResponseBytes(BLogger logger, CloseableHttpResponse response)
			throws BExceptions {
		try {
			if (response == null || response.getEntity() == null || response.getEntity().getContent() == null) {
				throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, "Empty response received from " + url);
			}
			if (response.getStatusLine() == null || !validStatusCodes.contains(response.getStatusLine().getStatusCode())) {
				throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR,
						"Call failed for " + url + " with http code "
								+ ((response.getStatusLine() == null) ? "" : response.getStatusLine().getStatusCode())
								+ " ReasonPhrase: " + response.getStatusLine().getReasonPhrase());
			}

			InputStream is = response.getEntity().getContent();
			if (is != null) {
				responseBytes = IOUtils.toByteArray(is);
			}

			if (respHeaders != null) {
				Header[] headers = response.getAllHeaders();
				for (Header hdr : headers) {
					respHeaders.put(hdr.getName(), hdr.getValue());
				}
			}

			EntityUtils.consume(response.getEntity());
			if (responseBytes == null || responseBytes.length == 0) {
				throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, "Empty result received from " + url);
			}
		} catch (IOException e) {
			logger.error(e);
			throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, url + " " + e.getMessage());
		}
	}

	public ResponseCapsuleImpl getByteResponseCapsule(BLogger logger) throws BExceptions {
		return new ResponseCapsuleImpl(responseBytes); 
	}
	
	public ResponseCapsuleImpl getResponseCapsule(BLogger logger, Class<?> respClass) throws BExceptions {
		if(responseBytes == null || responseBytes.length == 0) {
			throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, "Empty result received from " + url);
		}
		
		try {
			UTF8String utf8Str = new UTF8String(new String(responseBytes));
			String jsonStr = utf8Str.getUTF8String();
			Object respObj = null;
			if (respClass != null && jsonStr != null && !jsonStr.isBlank()) {
				BJson gson = new BJson();
				respObj = gson.fromJson(jsonStr, respClass);
			}
			return new ResponseCapsuleImpl(jsonStr, respObj);
		} catch (Exception e) {
			logger.error(e);
			throw new BExceptions(FwConstants.PCodes.REST_CONN_ERROR, url + " " + e.getMessage());
		}
	}

	public byte[] getResponseBytes() {
		return responseBytes;
	}
	
	public void copyResponseHeaders(Map<String, String> respHeaders) {
		if(respHeaders != null && !this.respHeaders.isEmpty()) {
			respHeaders.putAll(this.respHeaders);
		}
	}
	
	private static HttpRequest createHttpRequest(BLogger logger, String methodType, String url, String reqJSON)
			throws BExceptions {
		HttpRequest request = null;
		switch (methodType) {
		case "GET":
			request = new HttpGet(url);
			break;
		case "DELETE":
			request = new HttpDelete(url);
			break;
		case "POST":
			HttpPost pr = new HttpPost(url);
			request = pr;
			if (reqJSON != null) {
				pr.setEntity(new StringEntity(reqJSON, "UTF-8"));
			}
			break;
		case "PUT":
			HttpPut pp = new HttpPut(url);
			request = pp;
			if (reqJSON != null) {
				pp.setEntity(new StringEntity(reqJSON, "UTF-8"));
			}
			break;
		default:
			logger.error("Incompatible HTTP request method->" + methodType);
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Incompatible HTTP request method");

		}
		return request;
	}
}
