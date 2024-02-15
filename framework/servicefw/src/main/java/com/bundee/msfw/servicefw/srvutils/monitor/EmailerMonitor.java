package com.bundee.msfw.servicefw.srvutils.monitor;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.emaili.EmailMessage;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.logi.BLogger;

public class EmailerMonitor implements EmailerService {

	private EmailerService emailerService;
	private MonitoringTracker perfTracker;
	
	public EmailerMonitor(EmailerService emailerService, MonitoringTracker perfTracker) {
		this.emailerService = emailerService;
		this.perfTracker = perfTracker;
	}
	
	@Override
	public HealthDetails checkHealth(BLogger logger) {
		return emailerService.checkHealth(logger);
	}

	@Override
	public EmailMessage getNewEmailMessage() {
		return emailerService.getNewEmailMessage();
	}

	@Override
	public void sendEmail(BLogger logger, EmailMessage message) throws BExceptions {
		PerfMonData perfData = perfTracker.startTracking(MonEvent.EMAILER, emailerService.getClass().getSimpleName() + "::sendEmail");
		try {
			emailerService.sendEmail(logger, message);
			perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			perfTracker.endTracking(perfData, ex);
			throw ex;
		}
	}
}
