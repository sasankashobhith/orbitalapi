package com.bundee.msfw.interfaces.fcfgi;

public interface Application {
	String getName();
	String getThisHost();
	int getThisServicePort();

	String getConfFolder();
	String getCertsFolder();
	String getLogsFolder();
	
	String makeAbsolutePathFromRoot(String path);
	String makeAbsolutePathFromConf(String path);
	String makeAbsolutePathFromCert(String path);
	
	boolean isUsingResource();
}
