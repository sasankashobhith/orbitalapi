package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface RequestAuditor {
	void audit(BLogger logger, BLModServices blModServices, Object requestDTOObj, Object responsetDTOObj, BExceptions procErrors) throws BExceptions;
}
