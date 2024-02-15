package com.bundee.msfw.servicefw.srvutils.location;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.services.location.LocationDetails;

public class LocationDetailsImpl implements LocationDetails {
	private int id;
	private UTF8String city;
	private UTF8String state;
	private long createTS;	
	private long updateTS;	

	public int getID() {
		return id;
	}
	public UTF8String getCity() {
		return city;
	}
	public UTF8String getState() {
		return state;
	}
	public long getCreateTS() {
		return createTS;
	}
	public long getUpdateTS() {
		return updateTS;
	}
}
