package com.bundee.msfw.interfaces.logi;

import java.io.PrintStream;

public interface BLogger {

	void trace(String msg);
	void debug(String msg);
	void info(String msg);
	void error(String msg);
	void warn(String msg);
	void fatal(String msg);
	void error(Throwable ex);
	void event(String msg);

	public PrintStream getPS();	
	public String getModID();
}
