package com.bundee.msfw.servicefw.fw;

import java.io.File;

import com.bundee.msfw.interfaces.fcfgi.Application;

public class ApplicationDetails implements Application {

	String name = "unknown";
	String host = "unknown";
	int port = -1;
	String rootFolder = "";
	String confFolder = "";
	String certsFolder = "";
	String logsFolder = "";
	private static boolean bUsingResource = false;
	
	public ApplicationDetails() {
	}
	
	public void setName(String appName) {
		this.name = appName;
	}
	
	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}
	
	public void setConfFolder(String confFolder) {
		this.confFolder = confFolder;
	}

	public void setCertsFolder(String certsFolder) {
		this.certsFolder = certsFolder;
	}

	public void setLogsFolder(String logsFolder) {
		this.logsFolder = logsFolder;
	}

	public static void setUsingResource(boolean bur) {
		bUsingResource = bur;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getThisHost() {
		return host;
	}

	@Override
	public int getThisServicePort() {
		return port;
	}
	
	@Override
	public String getConfFolder() {
		return confFolder;
	}

	@Override
	public String getCertsFolder() {
		return certsFolder;
	}

	@Override
	public String getLogsFolder() {
		return logsFolder;
	}
	
	@Override
	public String makeAbsolutePathFromRoot(String path) {
		return makeAbsolutePath(rootFolder, path);
	}

	@Override
	public String makeAbsolutePathFromConf(String path) {
		return makeAbsolutePath(confFolder, path);
	}
	
	@Override
	public String makeAbsolutePathFromCert(String path) {
		return makeAbsolutePath(certsFolder, path);
	}
	
	private static String makeAbsolutePath(String certRoot, String path) {
		if(bUsingResource) return path;
		
		String resPath = path;
		File absPathChk = new File(path);
		if(!absPathChk.exists()) {
			resPath = certRoot + path;
		}
		
		return resPath;
	}

	@Override
	public boolean isUsingResource() {
		return bUsingResource;
	}	
}
