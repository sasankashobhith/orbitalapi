package com.bundee.msfw.interfaces.blmodi;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.interfaces.logi.BLogger;

import java.util.List;

public interface PageHandler {
	int getPageSize();
	List<Long> getFullDataIDs(BLogger logger);
	BaseResponse getDataFromIDs(BLogger logger, BLModServices blModServices, List<Long> ids4Page) throws BExceptions;
}
