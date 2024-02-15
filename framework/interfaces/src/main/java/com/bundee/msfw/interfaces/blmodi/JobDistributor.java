package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface JobDistributor {
	Object acquireLock(BLogger logger, String jobType) throws BExceptions;
	void releaseLock(BLogger logger, String jobType, Object lockID) throws BExceptions;
}
