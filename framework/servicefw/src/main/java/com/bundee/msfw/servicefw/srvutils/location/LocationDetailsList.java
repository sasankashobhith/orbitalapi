package com.bundee.msfw.servicefw.srvutils.location;

import java.util.List;

import com.bundee.msfw.defs.BaseResponse;

public class LocationDetailsList extends BaseResponse {
	List<LocationDetailsImpl> locations;

	public List<LocationDetailsImpl> getLocations() {
		return locations;
	}
}
