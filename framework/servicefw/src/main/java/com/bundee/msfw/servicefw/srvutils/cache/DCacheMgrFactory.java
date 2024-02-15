package com.bundee.msfw.servicefw.srvutils.cache;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class DCacheMgrFactory {
	private static final short DC_NO_CACHE = 0;
	private static final short DC_APP_LOCAL_CACHE = 1;
	private static final short DC_DIST_CACHE = 2;

	public static final short DC_SVC_TYPE_APIS = 1;
	public static final short DC_SVC_TYPE_JOBS = 2;

	DBaseCacheMgrImpl twtDCacheMgrImpl;
	short svcType;
	int loadingDelayMS = 0;
	short cfgSvcType;
	String cacheLoadedTestGrp;
	String cacheLoadedTestKey;
	String cacheLoadedTestValue;
	boolean bIsDistCache;
	
	public DCacheMgrFactory(short svcType, FileCfgHandler fch) {
		this.svcType = svcType;
		
		int val = fch.getCfgParamInt("dc.cache.usage");
		//short iObjects = (short)fch.getCfgParamInt("dc.cache.i.objects");
		//short sObjects = (short)fch.getCfgParamInt("dc.cache.s.objects");
		//short oObjects = (short)fch.getCfgParamInt("dc.cache.o.objects");

		switch(val) {
		case DC_NO_CACHE: 
			twtDCacheMgrImpl = null;
			bIsDistCache = false;
			break;
		case DC_APP_LOCAL_CACHE: 
			twtDCacheMgrImpl = new DLocalCacheMgrImpl();
			bIsDistCache = false;
			break;
		case DC_DIST_CACHE: 
			twtDCacheMgrImpl = null;
			bIsDistCache = true;
			break;
		}
		
		cfgSvcType = (short)fch.getCfgParamInt("dc.cache.loading.pref");
		loadingDelayMS = fch.getCfgParamInt("dc.cache.loading.delay.secs") * 1000;
		
		cacheLoadedTestGrp = fch.getCfgParamStr("dc.cache.test.group");
		cacheLoadedTestKey = fch.getCfgParamStr("dc.cache.test.key");
		cacheLoadedTestValue = fch.getCfgParamStr("dc.cache.test.value");
	}
	
	public DBaseCacheMgrImpl getCacheManagerImpl() {
		return twtDCacheMgrImpl;
	}
	
	public boolean isDistCache() {
		return bIsDistCache;
	}
	
	public void checkDCacheLoading(BLogger logger) throws BExceptions {
		if(twtDCacheMgrImpl == null || svcType == cfgSvcType || !bIsDistCache) return;
		
		String val = twtDCacheMgrImpl.getGlobalCacheR().getStrValue(cacheLoadedTestGrp, cacheLoadedTestKey);
		if(val == null || !val.equalsIgnoreCase(cacheLoadedTestValue)) {
			try {
				Thread.sleep(loadingDelayMS);
			} catch (InterruptedException e) {
			}
			val = twtDCacheMgrImpl.getGlobalCacheR().getStrValue(cacheLoadedTestGrp, cacheLoadedTestKey);
			if(val == null || !val.equalsIgnoreCase(cacheLoadedTestValue)) {
				throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "Distributed cache is not loaded by the predecessor app");
			}
		}
	}
	
	public void updateDCacheLoading(BLogger logger) throws BExceptions {
		if(twtDCacheMgrImpl == null || svcType == cfgSvcType || !bIsDistCache) return;
		
		String val = twtDCacheMgrImpl.getGlobalCacheR().getStrValue(cacheLoadedTestGrp, cacheLoadedTestKey);
		if(val == null || !val.equalsIgnoreCase(cacheLoadedTestValue)) {
			twtDCacheMgrImpl.getGlobalCacheW().setStrValue(cacheLoadedTestGrp, cacheLoadedTestKey, cacheLoadedTestValue);
		}
	}

}
