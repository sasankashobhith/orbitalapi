package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface BLModule {
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions;
	default public boolean authzRequired() { return true; };
}
