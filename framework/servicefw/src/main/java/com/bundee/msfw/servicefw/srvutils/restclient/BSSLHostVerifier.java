package com.bundee.msfw.servicefw.srvutils.restclient;

import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

public class BSSLHostVerifier implements HostnameVerifier {
	
	Set<String> loopbackHosts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	HostnameVerifier defHostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
	
	public BSSLHostVerifier() {
		loopbackHosts.add("localhost");
		loopbackHosts.add("127.0.0.1");
		loopbackHosts.add("::1");
	}

	@Override
	public boolean verify(String hostname, SSLSession session) {
		if(loopbackHosts.contains(hostname)) {
			return true;
		}
		return defHostnameVerifier.verify(hostname, session);
	}
}
