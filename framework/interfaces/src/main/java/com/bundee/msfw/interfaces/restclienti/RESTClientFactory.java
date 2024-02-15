package com.bundee.msfw.interfaces.restclienti;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface RESTClientFactory {
	RESTClient getNewRESTClient(BLogger logger, String clientID, String configPfx, BLModServices blModServices) throws BExceptions;
	Object getSSLFactory(BLogger logger, String clientID, String configPfx, BLModServices blModServices) throws BExceptions;
}
