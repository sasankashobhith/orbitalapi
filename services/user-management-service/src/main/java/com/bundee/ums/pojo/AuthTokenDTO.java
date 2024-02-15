package com.bundee.ums.pojo;

import com.bundee.msfw.defs.BaseResponse;

public class AuthTokenDTO extends BaseResponse {
	String authToken;
	
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
}
