package com.bundee.msfw.servicefw.fw;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.CustomService;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.os.ObjectStoreService;
import com.bundee.msfw.interfaces.restclienti.RESTClientFactory;
import com.bundee.msfw.interfaces.utili.UtilFactory;
import com.bundee.msfw.interfaces.utili.dc.DCacheManagerR;
import com.bundee.msfw.interfaces.utili.dc.DCacheManagerW;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.dbm.DBManagerFactory;
import com.bundee.msfw.servicefw.srvutils.cache.DBaseCacheMgrImpl;
import com.bundee.msfw.servicefw.srvutils.monitor.CustomMonitor;
import com.bundee.msfw.servicefw.srvutils.monitor.DBMonitor;
import com.bundee.msfw.servicefw.srvutils.monitor.EmailerMonitor;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;
import com.bundee.msfw.servicefw.srvutils.monitor.ObjStoreMonitor;
import com.bundee.msfw.servicefw.srvutils.monitor.VaultMonitor;
import com.bundee.msfw.servicefw.srvutils.restclient.RESTClientIniterFactoryImpl;
import com.bundee.msfw.servicefw.srvutils.restclient.RESTClientRuntimeFactoryImpl;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import com.bundee.msfw.servicefw.srvutils.utils.TLSDataCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.ThreadLocalData;
import com.bundee.msfw.servicefw.srvutils.utils.UtilFactoryImpl;
import com.bundee.msfw.services.location.LocationService;

public class BLModServicesImpl implements BLModServices {
	DBaseCacheMgrImpl cacheManager;
	VaultService vaultService;
	LocationService locationService;

	UtilFactoryImpl utilFactoryImpl;
	DBManager dbm;
	Map<String, DBManager> allMonitoredDBMs = new TreeMap<String, DBManager>(String.CASE_INSENSITIVE_ORDER);

	ObjectStoreService objectStoreService;
	EmailerService emailerService;

	RESTClientIniterFactoryImpl restClientFactoryImpl;
	RESTClientRuntimeFactoryImpl restClientRuntimeFactoryImpl;
	Map<String, CustomService> customServices;
	FileCfgHandler fch;

	MonitoringTracker monTracker;

	public BLModServicesImpl(GlobalServiceCapsule globalServiceCapsule) {
		monTracker = new MonitoringTracker(globalServiceCapsule.bPerfMonEnabled, globalServiceCapsule.bSvcMonEnabled,
				"initialization", "initialization");
		init(globalServiceCapsule);
	}

	public BLModServicesImpl(GlobalServiceCapsule globalServiceCapsule, String eventID, String sessionID) {
		monTracker = new MonitoringTracker(globalServiceCapsule.bPerfMonEnabled, globalServiceCapsule.bSvcMonEnabled,
				eventID, sessionID);
		init(globalServiceCapsule);
	}

	private void init(GlobalServiceCapsule globalServiceCapsule) {
		this.cacheManager = globalServiceCapsule.getCacheManager();

		this.vaultService = initMemberService(globalServiceCapsule, globalServiceCapsule.vaultService,
				VaultService.class, VaultMonitor.class);
		this.objectStoreService = initMemberService(globalServiceCapsule, globalServiceCapsule.getObjectStoreService(),
				ObjectStoreService.class, ObjStoreMonitor.class);
		this.emailerService = initMemberService(globalServiceCapsule, globalServiceCapsule.getEmailerService(),
				EmailerService.class, EmailerMonitor.class);
		this.locationService = globalServiceCapsule.getLocationService();

		DBManagerFactory dbmf = globalServiceCapsule.getDBMFactory();
		if (dbmf != null) {
			Map<String, DBManager> allDBMs = dbmf.getAllDBManagers();

			this.dbm = initMemberService(globalServiceCapsule, dbmf.getDBM(), DBManager.class, DBMonitor.class);

			allDBMs.entrySet().forEach(pair -> {
				DBManager monDBM = initMemberService(globalServiceCapsule, pair.getValue(), DBManager.class,
						DBMonitor.class);
				allMonitoredDBMs.put(pair.getKey(), monDBM);
			});
		}
		this.utilFactoryImpl = new UtilFactoryImpl();

		this.restClientFactoryImpl = globalServiceCapsule.getRESTClientIniterFactoryImpl();
		if (restClientFactoryImpl == null && (globalServiceCapsule.getRESTClients() != null
				&& !globalServiceCapsule.getRESTClients().isEmpty())) {
			this.restClientRuntimeFactoryImpl = new RESTClientRuntimeFactoryImpl(globalServiceCapsule.bPerfMonEnabled,
					globalServiceCapsule.getRESTClients(), monTracker);
		}

		if (globalServiceCapsule.customServices != null && !globalServiceCapsule.customServices.isEmpty()) {
			this.customServices = new HashMap<String, CustomService>();
			globalServiceCapsule.customServices.entrySet().forEach(entry -> {
				this.customServices.put(entry.getKey(), initMemberService(globalServiceCapsule, entry.getValue(),
						CustomService.class, CustomMonitor.class));
			});
		}

		ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
		if(tld != null) {
			tld.setMonitoringTracker(monTracker);
		}
		this.fch = globalServiceCapsule.fch;
	}

	@SuppressWarnings("unchecked")
	private <C> C initMemberService(GlobalServiceCapsule gss, C member, Class<?> svcInterfaceClass, Class<?> monClass) {
		C newMember = null;
		if (member == null || !gss.bPerfMonEnabled) {
			newMember = member;
		} else {
			Class<?>[] cargs = new Class<?>[2];
			cargs[0] = svcInterfaceClass;
			cargs[1] = monTracker.getClass();

			try {
				newMember = (C) monClass.getDeclaredConstructor(cargs).newInstance(member, monTracker);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			}
		}

		return newMember;
	}

	public VaultService getVaultService() {
		return vaultService;
	}

	public MonitoringTracker getMonTracker() {
		return monTracker;
	}

	@Override
	public DBManager getDBManager() throws BExceptions {
		if (dbm != null) {
			return dbm;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "Default Database is not configured!");
	}

	@Override
	public UtilFactory getUtilFactory() throws BExceptions {
		if (utilFactoryImpl != null) {
			return utilFactoryImpl;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "UtilFactory is not configured!");
	}

	@Override
	public DCacheManagerR getCacheManagerR() throws BExceptions {
		if (cacheManager != null) {
			return cacheManager;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "CacheManager is not configured!");
	}

	@Override
	public DCacheManagerW getCacheManagerW() throws BExceptions {
		if (cacheManager != null) {
			return cacheManager;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "CacheManager is not configured!");
	}

	@Override
	public ObjectStoreService getObjectStoreService() throws BExceptions {
		if (objectStoreService != null) {
			return objectStoreService;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "ObjectStoreService is not configured!");
	}

	@Override
	public EmailerService getEmailerService() throws BExceptions {
		if (emailerService != null) {
			return emailerService;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "EmailerService is not configured!");
	}

	@Override
	public LocationService getLocationService() throws BExceptions {
		if (locationService != null) {
			return locationService;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "LocationService is not configured!");
	}
	
	@Override
	public RESTClientFactory getRESTClientFactory() throws BExceptions {
		if (restClientFactoryImpl != null) {
			return restClientFactoryImpl;
		} else if (restClientRuntimeFactoryImpl != null) {
			return restClientRuntimeFactoryImpl;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "RESTClientFactory is not configured!");
	}

	@Override
	public CustomService getCustomService(String csName) throws BExceptions {
		if (customServices != null && customServices.containsKey(csName)) {
			return customServices.get(csName);
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, csName + " is not configured!");
	}

	public Map<String, CustomService> getAllCustomServices() throws BExceptions {
		if (customServices != null && !customServices.isEmpty()) {
			return customServices;
		}
		throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "No custom services are configured!");
	}

	@Override
	public FileCfgHandler getFileCfgHandler() throws BExceptions {
		return fch;
	}
}
