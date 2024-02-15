package com.bundee.msfw.servicefw.jobs;

import java.util.Set;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.blmodi.RecurringJob;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.BLModServicesImpl;
import com.bundee.msfw.servicefw.fw.GlobalServiceCapsule;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringHelper;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;

public class HealthCheckJob implements RecurringJob {
	GlobalServiceCapsule gsc;
	MonitoringHelper monitoringHelper;

	public HealthCheckJob() {
	}
	
	public HealthCheckJob(GlobalServiceCapsule gsc, Set<HealthCheck> modules) {
		this.gsc = gsc;
		this.monitoringHelper = new MonitoringHelper(modules);
	}

	@Override
	public String getJobType() {
		return "health-check";
	}
	
	@Override
	public boolean useDistributor() {
		return false;
	}

	@Override
	public void init(BLogger logger, BLModServices blModServices) {
	}

	@Override
	public void execute(BLogger logger, BLModServices blModServices) throws BExceptions {
		BLModServicesImpl blModServicesImpl = (BLModServicesImpl) blModServices;
		BLModServicesImpl monBLModServices = new BLModServicesImpl(gsc, blModServicesImpl.getMonTracker().getEventID(), blModServicesImpl.getMonTracker().getSessionID());
		monitoringHelper.performMonitoring(logger, monBLModServices);
		MonitoringTracker monTracker = monBLModServices.getMonTracker();
		monTracker.finalizeSM(logger, gsc.getFileCfgHandler().getApplication().getThisHost(), gsc.getDBMFactory().getDBM());
		MonitoringHelper.addTrack(monTracker);
	}

}
