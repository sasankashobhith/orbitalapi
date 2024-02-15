package com.bundee.msfw.interfaces.emaili;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface EmailerService extends HealthCheck {
	public EmailMessage getNewEmailMessage();
	public void sendEmail(BLogger logger, EmailMessage message) throws BExceptions;
}
