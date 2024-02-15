package com.bundee.testsvc.pojo;

import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.HealthDetails;

public class SelfTestResponse extends BaseResponse {
	HealthDetails health;
	
	public void setHealthDetails(HealthDetails health) {
		this.health = health;
	}
}
