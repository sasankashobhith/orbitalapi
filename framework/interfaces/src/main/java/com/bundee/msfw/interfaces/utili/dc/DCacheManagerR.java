package com.bundee.msfw.interfaces.utili.dc;

import com.bundee.msfw.defs.BExceptions;

public interface DCacheManagerR {
	DCacheReader getGlobalCacheR() throws BExceptions;
	DCacheReader getTenantCacheR(int tenantID) throws BExceptions;
}
