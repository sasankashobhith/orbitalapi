package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface HealthCheck {
	HealthDetails checkHealth(BLogger logger);
}
