package com.bundee.msfw.servicefw.srvutils.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.bundee.msfw.interfaces.fcfgi.Application;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.servicefw.fw.ApplicationDetails;

public class FileCfgHandlerImpl implements FileCfgHandler {
	private Properties globalCfgs = new Properties();
	private Map<String, Object> allCfgs = new HashMap<String, Object>();
	private ApplicationDetails appDetails;
	
	public FileCfgHandlerImpl() {
		String hostName = "unknown";
		try {
			hostName = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = "unknown";
		}
		appDetails = new ApplicationDetails();
		appDetails.setHost(hostName);
	}
	
	public void setAppName(String appName) {
		appDetails.setName(appName);
	}
	
	public long getCfgParamLong(String pn) {
		long pv = -1;
		if(pn == null) return pv;
		
		try {
			String pvs = globalCfgs.getProperty(pn);
			if(pvs != null) {
				pv = Long.parseLong(pvs);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pv;
	}
	
	public int getCfgParamInt(String pn) {
		int pv = -1;
		if(pn == null) return pv;
		
		try {
			String pvs = globalCfgs.getProperty(pn);
			if(pvs != null) {
				pv = Integer.parseInt(pvs);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pv;
	}

	public String getCfgParamStr(String pn) {
		String pvs = "";
		if(pn == null) return pvs;
		
		try {
			if(pvs != null) {
				pvs = globalCfgs.getProperty(pn);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pvs;
	}
	
	public Map<String, Object> getAllCfgParams() {
		return allCfgs;
	}

	public Map<String, Object> getAllCfgParams(String pfx) {
		if(pfx == null || pfx.isBlank()) return allCfgs;
		Map<String, Object> filteredMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		
		allCfgs.entrySet().forEach( pair -> {
			if(pair.getKey().startsWith(pfx)) {
				filteredMap.put(pair.getKey(), pair.getValue());
			}
		});
		return filteredMap;
	}
	
	public void addCfgParam(String key, Object value) {
		this.allCfgs.put(key, value);
		this.globalCfgs.put(key, value);
	}
	
	public void loadPropertiesFile(String filePath) {
		try {
			System.out.println("Reading configurations from: " + filePath);
			InputStream is = new FileInputStream(filePath);
			globalCfgs.load(is);
			
			for(Map.Entry<Object, Object> oentry : globalCfgs.entrySet()) {
				allCfgs.put((String)oentry.getKey(), oentry.getValue());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void loadPropertiesFile(InputStream is) {
		try {
			globalCfgs.load(is);
			
			for(Map.Entry<Object, Object> oentry : globalCfgs.entrySet()) {
				allCfgs.put((String)oentry.getKey(), oentry.getValue());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public Application getApplication() {
		return appDetails;
	}
}
