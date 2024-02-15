package com.bundee.msfw.interfaces.emaili;

import java.util.Collection;

import com.bundee.msfw.defs.UTF8String;

public interface EmailMessage {
	public void add2TOList(UTF8String recipient);
	public void add2CCList(UTF8String recipient);
	public void add2BCCList(UTF8String recipient);
	public EmailAttachment getNewEmailAttachment();
	public EmailAttachment getNewInlineAttachment();
	public void setSubject(UTF8String subject); 
	public void setBodyText(UTF8String bodyText); 
	
	public Collection<UTF8String> getTOList();
	public Collection<UTF8String> getCCList();
	public Collection<UTF8String> getBCCList();
	public UTF8String getSubject(); 
	public UTF8String getBodyText(); 
	public Collection<EmailAttachment> getAttachments();
	public Collection<EmailAttachment> getInlineAttachments();
}
