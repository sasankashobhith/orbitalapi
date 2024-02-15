package com.bundee.msfw.servicefw.srvutils.monitor;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.vault.VaultService;

public class VaultMonitor implements VaultService {
	private VaultService vaultService;
	private MonitoringTracker perfTracker;
	
	public VaultMonitor(VaultService vaultService, MonitoringTracker perfTracker) {
		this.vaultService = vaultService;
		this.perfTracker = perfTracker;
	}
	
	@Override
	public String getValue(BLogger logger, String key) throws BExceptions {
		PerfMonData perfData = perfTracker.startTracking(MonEvent.VAULT, vaultService.getClass().getSimpleName() + "::getValue");
		String val = null;
		try {
			val = vaultService.getValue(logger, key);
			perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			perfTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public HealthDetails checkHealth(BLogger logger) {
		return vaultService.checkHealth(logger);
	}
}
