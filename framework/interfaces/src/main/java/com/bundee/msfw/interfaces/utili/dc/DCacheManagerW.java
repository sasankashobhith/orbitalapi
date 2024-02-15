package com.bundee.msfw.interfaces.utili.dc;

import com.bundee.msfw.defs.BExceptions;

public interface DCacheManagerW {
	DCacheWriter getGlobalCacheW() throws BExceptions;
	DCacheWriter getTenantCacheW(int tenantID) throws BExceptions;
}
