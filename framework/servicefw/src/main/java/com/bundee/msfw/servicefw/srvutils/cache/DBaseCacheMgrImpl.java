package com.bundee.msfw.servicefw.srvutils.cache;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.utili.dc.DCacheManagerR;
import com.bundee.msfw.interfaces.utili.dc.DCacheManagerW;
import com.bundee.msfw.interfaces.utili.dc.DCacheReader;
import com.bundee.msfw.interfaces.utili.dc.DCacheWriter;

//Exisists just as a reference holder
public class DBaseCacheMgrImpl implements DCacheManagerR, DCacheManagerW {

	@Override
	public DCacheWriter getGlobalCacheW() throws BExceptions {
		return null;
	}

	@Override
	public DCacheWriter getTenantCacheW(int tenantID) throws BExceptions {
		return null;
	}

	@Override
	public DCacheReader getGlobalCacheR() throws BExceptions {
		return null;
	}

	@Override
	public DCacheReader getTenantCacheR(int tenantID) throws BExceptions {
		return null;
	}
}
