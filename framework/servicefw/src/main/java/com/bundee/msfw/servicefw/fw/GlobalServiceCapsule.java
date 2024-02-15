package com.bundee.msfw.servicefw.fw;

import java.util.Map;
import java.util.TreeMap;

import com.bundee.msfw.interfaces.blmodi.CustomService;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.os.ObjectStoreService;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.dbm.DBManagerFactory;
import com.bundee.msfw.servicefw.srvutils.cache.DBaseCacheMgrImpl;
import com.bundee.msfw.servicefw.srvutils.restclient.RESTClientIniterFactoryImpl;
import com.bundee.msfw.servicefw.srvutils.utils.UtilFactoryImpl;
import com.bundee.msfw.services.location.LocationService;

public class GlobalServiceCapsule {
	private static GlobalServiceCapsule svcCapsule = new GlobalServiceCapsule();
	public static GlobalServiceCapsule getInstance() {
		return svcCapsule;
	}

	VaultService vaultService;
	LocationService locationService;
	
	FileCfgHandler fch;
	
	DBaseCacheMgrImpl cacheManager;
	DBManagerFactory dbmf;
	ObjectStoreService objectStoreService;
	EmailerService emailerService;
	
	UtilFactoryImpl utilFactoryImpl = new UtilFactoryImpl();
	RESTClientIniterFactoryImpl restClientIniterFactoryImpl = new RESTClientIniterFactoryImpl();
	
	Map<String, RESTClient> restClients;

	Map<String, CustomService> customServices;
	
	boolean bPerfMonEnabled;
	boolean bSvcMonEnabled;
	boolean bCertMonEnabled;
	
	public DBaseCacheMgrImpl getCacheManager() {
		return cacheManager;
	}
	public UtilFactoryImpl getUtilFactoryImpl() {
		return utilFactoryImpl;
	}
	
	public FileCfgHandler getFileCfgHandler() {
		return fch;
	}
	
	public DBManagerFactory getDBMFactory() {
		return dbmf;
	}
	
	public VaultService getVaultService() {
		return vaultService;
	}
	
	public LocationService getLocationService() {
		return locationService;
	}
	
	public ObjectStoreService getObjectStoreService() {
		return objectStoreService;
	}

	public EmailerService getEmailerService() {
		return emailerService;
	}
	
	public RESTClientIniterFactoryImpl getRESTClientIniterFactoryImpl() {
		return restClientIniterFactoryImpl;
	}

	public Map<String, RESTClient> getRESTClients() {
		return restClients;
	}
	
	
	public void setCacheManager(DBaseCacheMgrImpl cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setUtilFactoryImpl(UtilFactoryImpl utilFactoryImpl) {
		this.utilFactoryImpl = utilFactoryImpl;
	}

	public void setService(Object serviceObj) {
		if(serviceObj instanceof VaultService) {
			this.vaultService = (VaultService) serviceObj;
		} else if(serviceObj instanceof ObjectStoreService) {
			this.objectStoreService = (ObjectStoreService) serviceObj;
		} else if(serviceObj instanceof EmailerService) {
			this.emailerService = (EmailerService) serviceObj;
		} else if(serviceObj instanceof LocationService) {
			this.locationService = (LocationService)serviceObj;
		}
	}
	
	public void resetRESTClientIniterFactoryImpl() {
		if(restClientIniterFactoryImpl != null) {
			restClients = restClientIniterFactoryImpl.getOtherRESTClients();
		}
		restClientIniterFactoryImpl = null;
	}
	
	public Map<String, CustomService> getCustomServices() {
		return customServices;
	}

	public void addCustomService(String name, CustomService customService) {
		if (customServices == null) {
			customServices = new TreeMap<String, CustomService>(String.CASE_INSENSITIVE_ORDER);
		}
		this.customServices.put(name, customService);
	}
}
