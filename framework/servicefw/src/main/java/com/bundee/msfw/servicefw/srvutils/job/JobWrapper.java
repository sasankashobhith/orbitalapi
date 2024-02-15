package com.bundee.msfw.servicefw.srvutils.job;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.JobDistributor;
import com.bundee.msfw.interfaces.blmodi.RecurringJob;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.BLModServicesImpl;
import com.bundee.msfw.servicefw.fw.GlobalServiceCapsule;
import com.bundee.msfw.servicefw.fw.ServiceFramework;
import com.bundee.msfw.servicefw.logger.BLoggerFactory;
import com.bundee.msfw.servicefw.srvutils.monitor.MonEvent;
import com.bundee.msfw.servicefw.srvutils.monitor.PerfMonData;
import com.bundee.msfw.servicefw.srvutils.utils.TLSDataCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.ThreadLocalData;

public class JobWrapper extends TimerTask {
	
	RecurringJob job;
	GlobalServiceCapsule gsc;
	JobReccurence jobReccurence;
	boolean bStopped = false;
	JobDistributor jobDistributor;
	private AtomicLong jobRunID = new AtomicLong(0);
	
	public JobWrapper(JobDistributor jobDistributor, RecurringJob job, JobReccurence jobReccurence, GlobalServiceCapsule gsc) {
		this.job = job;
		this.gsc = gsc;
		this.jobReccurence = jobReccurence;
		this.jobDistributor = jobDistributor;
	}
	
	public String getName() {
		return job.getClass().getSimpleName();
	}
	
	public void stop() {
		bStopped = true;
	}
	
	public JobReccurence getJobReccurence() {
		return jobReccurence;
	}

	@Override
	public void run() {
		if(bStopped) return;
		long runID = jobRunID.incrementAndGet();
		BLogger logger = BLoggerFactory.create("job", runID, job.getJobType());

		TLSDataCapsule.makeNewThreadLocalData();
		ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
		String runIDStr = Long.toString(runID);
		tld.setCallerReqID(logger, ServiceFramework.getInstance().getApplication(), runIDStr);
		
		boolean bAcquiredLock = false;
		Object lockID = null;
		BLModServicesImpl modServices = new BLModServicesImpl(gsc, Long.toString(runID), job.getClass().getSimpleName());
		
		PerfMonData pmd = null;
		try {
			if(job.useDistributor() && jobDistributor != null) {
				lockID = jobDistributor.acquireLock(logger, job.getJobType());
				bAcquiredLock = true;
			}
			pmd = modServices.getMonTracker().startTracking(MonEvent.JOB, job.getJobType() + "::" + job.getClass().getSimpleName());
			job.execute(logger, modServices);
			modServices.getMonTracker().endTracking(pmd);
		} catch (Throwable e) {
			modServices.getMonTracker().endTracking(pmd, e);
			logger.error(e);
		}
		if(bAcquiredLock) {
			try {
				jobDistributor.releaseLock(logger, job.getJobType(), lockID);
			} catch (BExceptions e) {
				logger.warn("Failed to release lock " + job.getJobType() + " : " + lockID);
			}
		}
		modServices.getMonTracker().finalizePM(logger, gsc.getFileCfgHandler().getApplication().getThisHost(), gsc.getDBMFactory().getDBM());
	}

}
