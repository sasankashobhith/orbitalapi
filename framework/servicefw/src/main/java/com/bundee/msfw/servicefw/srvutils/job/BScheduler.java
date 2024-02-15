package com.bundee.msfw.servicefw.srvutils.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.JobDistributor;
import com.bundee.msfw.interfaces.blmodi.RecurringJob;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.BLModServicesImpl;
import com.bundee.msfw.servicefw.fw.GlobalServiceCapsule;
import com.bundee.msfw.servicefw.logger.BLoggerFactory;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class BScheduler {
	private final int MIN_INIT_DELAY_SECS = 60; //1 min
	List<JobWrapper> jobsReady2Exec;
	List<Timer> jobsTimers;
	JobDistributor jobDistributor;
	int initialDelaySecs;

	public void init(BLogger logger, JobDistributor jobDistributor, int initialDelaySecs, GlobalServiceCapsule gsc,
			Map<String, JobReccurence> jobRecurrence, Map<String, RecurringJob> jobs, BExceptions exceptions) {
		this.initialDelaySecs = (initialDelaySecs < MIN_INIT_DELAY_SECS ? MIN_INIT_DELAY_SECS : initialDelaySecs);
		jobsReady2Exec = new ArrayList<JobWrapper>();
		BLModServicesImpl blModServices = new BLModServicesImpl(gsc);
		for (Map.Entry<String, RecurringJob> pair : jobs.entrySet()) {
			String jobClsName = pair.getKey();
			RecurringJob rjob = pair.getValue();
			JobReccurence jr = jobRecurrence.get(jobClsName); 
			if (jr.getRecTS() == null || jr.getRecTS() <= 0) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE,
						"Recurrence time configured for " + jobClsName);
			} else {
				BLogger jLogger = BLoggerFactory.create("job", 0L, rjob.getJobType());
				try {
					rjob.init(jLogger, blModServices);
					JobWrapper jw = new JobWrapper(jobDistributor, rjob, jr, gsc);
					jobsReady2Exec.add(jw);
				} catch (BExceptions e) {
					exceptions.add(e);
				}
			}
		}
	}

	public void start(BLogger logger) {
		jobsTimers = new ArrayList<Timer>();
		for (JobWrapper jw : jobsReady2Exec) {
			Timer timer = new Timer();
			jobsTimers.add(timer);
			long delyaMS = getInitialDelay(logger, jw.getName(), jw.jobReccurence, initialDelaySecs);
			timer.schedule(jw, delyaMS, jw.jobReccurence.getRecTS() * 1000);
		}
	}

	public void stop() {
		for (JobWrapper jw : jobsReady2Exec) {
			jw.stop();
		}
		for (Timer timer : jobsTimers) {
			timer.cancel();
		}
	}

	private static long getInitialDelay(BLogger logger, String jobName, JobReccurence jr, long initialDelaySecs) {
		long computedDelayMS = initialDelaySecs * 1000;
		Calendar curTime = Calendar.getInstance();
		long jrDelayMS = jr.getInitialDelayMS(curTime);
		if(jrDelayMS > 0) {
			computedDelayMS = jrDelayMS;
		}
		
		curTime.add(Calendar.MILLISECOND, (int)computedDelayMS);
		Date date = curTime.getTime();             
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		logger.info(jobName + " is going to start running from " + format.format(date));
		
		return computedDelayMS;
	}
	
	public static JobReccurence getJobRecInstance(String tvalue) throws BExceptions {
		return new JobReccurence(tvalue);
	}
}
