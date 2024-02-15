package com.bundee.msfw.servicefw.srvutils.utils;

import com.bundee.msfw.defs.UniversalConstants;

public class MessageContext {
	int tenantID;
	int baCode;
	String modID;
	String locale;

	public MessageContext() {
		baCode = 0;
		modID = "";
		locale = UniversalConstants.SYSTEM_DEFAULT_LOCALE;
	}
	
	public int getTenantID() {
		return tenantID;
	}
	public int getBACode() {
		return baCode;
	}
	public String getModID() {
		return modID;
	}
	public String getLocale() {
		return locale;
	}

	public void setTenantID(int tenantID) {
		this.tenantID = tenantID;
	}

	public void setBACode(int baCode) {
		this.baCode = baCode;
	}

	public void setModID(String modID) {
		this.modID = modID;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
