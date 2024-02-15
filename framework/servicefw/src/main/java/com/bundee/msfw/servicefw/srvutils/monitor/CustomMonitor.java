package com.bundee.msfw.servicefw.srvutils.monitor;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.CustomService;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;

public class CustomMonitor implements CustomService {

	private CustomService customService;
	private MonitoringTracker monTracker;

	public CustomMonitor(CustomService customService, MonitoringTracker monTracker) {
		this.customService = customService;
		this.monTracker = monTracker;
	}

	@Override
	public void init(BLogger logger, FileCfgHandler fch, BLModServices blModServices) throws BExceptions {
		customService.init(logger, fch, blModServices);
	}

	@Override
	public Object getValue(BLogger logger, String key) throws BExceptions {
		PerfMonData perfData = monTracker.startTracking(MonEvent.CUSTOM_SVC,
				customService.getClass().getSimpleName() + "::getValue");
		Object val = null;
		try {
			val = customService.getValue(logger, key);
			monTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			monTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public Object execute(BLogger logger, Object input) throws BExceptions {
		PerfMonData perfData = monTracker.startTracking(MonEvent.CUSTOM_SVC,
				customService.getClass().getSimpleName() + "::execute");
		Object val = null;
		try {
			val = customService.execute(logger, input);
			monTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			monTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public HealthDetails checkHealth(BLogger logger) {
		return customService.checkHealth(logger);
	}
}
