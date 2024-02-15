package com.bundee.msfw.interfaces.utili.concurrent;

import java.util.Collection;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface ConcurrentTaskExecutor {
	void execute(BLogger logger, Collection<ConcurrentTask> tasks) throws BExceptions;
}
