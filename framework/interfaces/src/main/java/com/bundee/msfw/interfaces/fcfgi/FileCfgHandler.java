package com.bundee.msfw.interfaces.fcfgi;

import java.util.Map;

public interface FileCfgHandler {
	public abstract long getCfgParamLong(String pn);
	public abstract int getCfgParamInt(String pn);
	public abstract String getCfgParamStr(String pn);
	public abstract Map<String, Object> getAllCfgParams();
	public abstract Map<String, Object> getAllCfgParams(String pfx);
	public Application getApplication();
}
