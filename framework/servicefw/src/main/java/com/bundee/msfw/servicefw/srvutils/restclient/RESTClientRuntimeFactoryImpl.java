package com.bundee.msfw.servicefw.srvutils.restclient;

import java.util.HashMap;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.restclienti.RESTClientFactory;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class RESTClientRuntimeFactoryImpl implements RESTClientFactory {
	private Map<String, RESTClient> restClients;

	public RESTClientRuntimeFactoryImpl() {
	}

	public RESTClientRuntimeFactoryImpl(boolean bPerfMonEnabled, Map<String, RESTClient> restClients,
			MonitoringTracker monTracker) {
		if (restClients != null && !restClients.isEmpty()) {
			this.restClients = new HashMap<String, RESTClient>();
			restClients.entrySet().forEach(entry -> {
				this.restClients.put(entry.getKey(), entry.getValue());
			});
		}
	}

	@Override
	public RESTClient getNewRESTClient(BLogger logger, String clientID, String configPfx, BLModServices blModServices)
			throws BExceptions {
		if (clientID == null || clientID.isBlank()) {
			throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "clientID is mandatory!");
		}

		RESTClient restClient = null;
		if (restClients.containsKey(clientID)) {
			restClient = restClients.get(clientID);
		} else {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE,
					"RESTClient not initialized for clientID " + clientID);
		}

		return restClient;
	}

	@Override
	public Object getSSLFactory(BLogger logger, String clientID, String configPfx, BLModServices blModServices)
			throws BExceptions {
		throw new BExceptions(FwConstants.PCodes.REQUEST_NOT_SUPPORTED,
				"SSLFactory can't be returned post-initialization");
	}

	public Map<String, RESTClient> getAllRESTClients() throws BExceptions {
		if (restClients != null && !restClients.isEmpty()) {
			return restClients;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "Non-default RESTClient are not initialized!");
	}
}
