package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface RecurringJob {
	public String getJobType();
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions;
	public void execute(BLogger logger, BLModServices blModServices) throws BExceptions;
	public boolean useDistributor();
}
