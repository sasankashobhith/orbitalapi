package com.bundee.msfw.interfaces.utili.concurrent;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface ConcurrentTask {
	String getUniqueTaskID();
	void runConcurrent(BLogger logger) throws BExceptions;
}
