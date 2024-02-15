package com.bundee.ums.utils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.ums.pojo.PushNotificationsResponse;

public class PushNotificationsUtill {
	
	

	public static PushNotificationsResponse createSinglePushNotification(PushNotificationsResponse row){
		PushNotificationsResponse res = new PushNotificationsResponse();

		res.setId(row.getId());
		res.setUserid(row.getUserid());
		res.setDevicetoken(row.getDevicetoken());
		res.setIsactive(row.getisIsactive());
		
		return res;

	}
	
	
	

}
