package com.bundee.msfw.servicefw.srvutils.location;

import java.util.ArrayList;
import java.util.Collection;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.defs.Utils;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import com.bundee.msfw.servicefw.srvutils.utils.ServiceIniter;
import com.bundee.msfw.servicefw.srvutils.utils.Utilities;
import com.bundee.msfw.services.location.LocationDetails;
import com.bundee.msfw.services.location.LocationService;

public class LocationServiceImpl implements LocationService, ServiceIniter {
	private static final String REST_CLIENT_ID = "location.client";
	private static final String BASE_URL_CFG = "location.client.baseURL";
	private RESTClient restClient;
	
	private String baseURL = "";
	
	@Override
	public void init(BLogger logger, FileCfgHandler fch, VaultService vaultService, BLModServices blModServices)
			throws BExceptions {
		restClient = blModServices.getRESTClientFactory().getNewRESTClient(logger, REST_CLIENT_ID, REST_CLIENT_ID, blModServices);
		baseURL = blModServices.getFileCfgHandler().getCfgParamStr(BASE_URL_CFG);
	}

	@Override
	public Collection<LocationDetails> getLocationsByCityState(BLogger logger, UTF8String city, UTF8String state) throws BExceptions {
		BExceptions exceptions = new BExceptions();
		if(Utils.isNullOrEmptyOrBlank(city)) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, "City is not valid!");
		}
		if(Utils.isNullOrEmptyOrBlank(state)) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, "State is not valid!");
		}
		if(exceptions.hasExceptions()) throw exceptions;
		
		String url = baseURL + "api/v1/location/search?city=" + city.getUTF8String() + "&state=" + state.getUTF8String();
		LocationDetailsList locList = (LocationDetailsList)restClient.sendReceive(logger, UniversalConstants.GET, url, null, null, LocationDetailsList.class, null);
		
		Collection<LocationDetails> locations = new ArrayList<LocationDetails>();
		
		if(locList != null) {
			if(locList.getLocations() != null) {
				locList.getLocations().forEach(loc -> locations.add(loc));
			} else {
				Utilities.extractErrCodes(locList.getCodes(), exceptions);
			}
		}
		
		if(exceptions.hasExceptions()) throw exceptions;
		
		return locations;
	}
}
