package com.bundee.msfw.interfaces.blmodi;

import java.util.List;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface BLAuthzModule {
	public TokenDetails validateToken(BLogger logger, BLModServices blModServices, Map<String, List<String>> headers) throws BExceptions;
	public void validatePermission(BLogger logger, BLModServices blModServices, TokenDetails tokenDetails, String permission) throws BExceptions;
}
