package com.bundee.msfw.servicefw.fw;

import com.bundee.msfw.interfaces.blmodi.TokenDetails;

public class DummyTokenDetails implements TokenDetails {
	
	public static DummyTokenDetails dtd = new DummyTokenDetails();

	@Override
	public Boolean isUserToken() {
		return true;
	}

	@Override
	public Integer getUserID() {
		return 0;
	}
}
