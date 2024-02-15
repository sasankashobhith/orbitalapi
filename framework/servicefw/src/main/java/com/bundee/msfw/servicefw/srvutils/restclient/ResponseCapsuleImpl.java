package com.bundee.msfw.servicefw.srvutils.restclient;

import com.bundee.msfw.interfaces.restclienti.ResponseCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.BJson;

public class ResponseCapsuleImpl implements ResponseCapsule {

	String jsonString;
	Object respObject;
	byte[] respBytes;
	int respSize = -1;
	
	ResponseCapsuleImpl(String jsonString, Object respObject) {
		this.jsonString = jsonString;
		this.respObject = respObject;
		this.respSize = (this.jsonString != null ? this.jsonString.length() : -1); 
	}

	ResponseCapsuleImpl(byte[] respBytes) {
		this.jsonString = null;
		this.respObject = null;
		this.respBytes = respBytes;
		this.respSize = (this.respBytes != null ? this.respBytes.length : -1); 
	}
	
	@Override
	public Object getResponseObject() {
		return respObject;
	}

	@Override
	public Object getErrorObject(Class<?> errClass) {
		BJson gson = new BJson(); 
		return gson.fromJson(jsonString, errClass);
	}
	@Override
	public byte[] getResponseBytes() {
		return respBytes;
	}

	@Override
	public int getRespSize() {
		return respSize;
	}

}
