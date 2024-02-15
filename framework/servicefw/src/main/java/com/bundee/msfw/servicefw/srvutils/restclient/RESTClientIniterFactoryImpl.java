package com.bundee.msfw.servicefw.srvutils.restclient;

import java.util.HashMap;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.restclienti.RESTClientFactory;
import com.bundee.msfw.servicefw.fw.BLModServicesImpl;
import com.bundee.msfw.servicefw.srvutils.monitor.RESTClientMonitor;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class RESTClientIniterFactoryImpl implements RESTClientFactory {
	private Map<String, RESTClient> restClients;
	private boolean bInitPhaseDone = false;
	
	public RESTClientIniterFactoryImpl() {
		restClients = new HashMap<String, RESTClient>(); 
	}

	public Map<String, RESTClient> getOtherRESTClients() {
		return restClients;
	}
	
	@Override
	public RESTClient getNewRESTClient(BLogger logger, String clientID, String configPfx, BLModServices blModServices) throws BExceptions {
		if(clientID == null || clientID.isBlank()) {
			throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "clientID is mandatory!");
		}
		
		RESTClient restClient = null;
		BLModServicesImpl blModServicesImpl = (BLModServicesImpl) blModServices;
		if(restClients.containsKey(clientID)) {
			restClient = restClients.get(clientID);
		} else if(!bInitPhaseDone){
			RESTClientImpl restClientImpl = new RESTClientImpl(logger, clientID, configPfx, blModServicesImpl.getFileCfgHandler(), blModServicesImpl.getVaultService());
			restClient = new RESTClientMonitor(restClientImpl);
			restClients.put(clientID, restClient);
		} else {
			throw new BExceptions(FwConstants.PCodes.REQUEST_NOT_SUPPORTED, "Initialization allowed only during startup " + configPfx);
		}
		
		return restClient;
	}

	@Override
	public Object getSSLFactory(BLogger logger, String clientID, String configPfx, BLModServices blModServices) throws BExceptions {
		BLModServicesImpl blModServicesImpl = (BLModServicesImpl) blModServices;
		RESTClientImpl restClientImpl = null;
		if(!bInitPhaseDone){
			restClientImpl = new RESTClientImpl(logger, clientID, configPfx, blModServicesImpl.getFileCfgHandler(), blModServicesImpl.getVaultService());
			RESTClientMonitor restClientMon = new RESTClientMonitor(restClientImpl);
			restClients.put(clientID, restClientMon);
			restClientMon.checkHealth(logger);
		} else {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Value of clientID is not valid " + clientID);
		}
		
		return restClientImpl.getSSLFactory();
	}
}
