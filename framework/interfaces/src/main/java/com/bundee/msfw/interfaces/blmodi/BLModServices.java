package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.os.ObjectStoreService;
import com.bundee.msfw.interfaces.restclienti.RESTClientFactory;
import com.bundee.msfw.interfaces.utili.UtilFactory;
import com.bundee.msfw.interfaces.utili.dc.DCacheManagerR;
import com.bundee.msfw.interfaces.utili.dc.DCacheManagerW;
import com.bundee.msfw.services.location.LocationService;

public interface BLModServices {
    DBManager getDBManager() throws BExceptions;
   
    DCacheManagerR getCacheManagerR() throws BExceptions;

    DCacheManagerW getCacheManagerW() throws BExceptions;

    UtilFactory getUtilFactory() throws BExceptions;

    RESTClientFactory getRESTClientFactory() throws BExceptions;

    ObjectStoreService getObjectStoreService() throws BExceptions;

    EmailerService getEmailerService() throws BExceptions;
    
    LocationService getLocationService() throws BExceptions;
    
    CustomService getCustomService(String csName) throws BExceptions;
    
    FileCfgHandler getFileCfgHandler() throws BExceptions;
}
