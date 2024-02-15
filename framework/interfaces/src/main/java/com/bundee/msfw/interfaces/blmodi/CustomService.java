package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface CustomService extends HealthCheck {
	void init(BLogger logger, FileCfgHandler fch, BLModServices blModServices) throws BExceptions;
	
	
	Object getValue(BLogger logger, String key)  throws BExceptions;
	Object execute(BLogger logger, Object input) throws BExceptions;
}
