package com.bundee.msfw.servicefw.srvutils.email;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.emaili.EmailAttachment;
import com.bundee.msfw.interfaces.emaili.EmailMessage;

public class EmailMessageImpl implements EmailMessage {
	Set<UTF8String> toList;
	Set<UTF8String> ccList;
	Set<UTF8String> bccList;
	UTF8String subject;
	UTF8String bodyText;
	Set<EmailAttachment> attachments;
	Set<EmailAttachment> inlineAttachments;
	
	public EmailMessageImpl() {
		toList = new HashSet<UTF8String>();
		ccList = new HashSet<UTF8String>();
		bccList = new HashSet<UTF8String>();
		attachments = new HashSet<EmailAttachment>();
		inlineAttachments = new HashSet<EmailAttachment>();
		bodyText = subject = new UTF8String("");
	}
	
	@Override
	public void add2TOList(UTF8String recipient) {
		if(recipient != null) {
			toList.add(recipient);
		}
	}

	@Override
	public void add2CCList(UTF8String recipient) {
		if(recipient != null) {
			ccList.add(recipient);
		}
	}

	@Override
	public void add2BCCList(UTF8String recipient) {
		if(recipient != null) {
			bccList.add(recipient);
		}
	}

	@Override
	public EmailAttachment getNewEmailAttachment() {
		EmailAttachment ea = new EmailAttachmentImpl();
		attachments.add(ea);
		return ea;
	}

	@Override
	public EmailAttachment getNewInlineAttachment() {
		EmailAttachment ia = new EmailAttachmentImpl();
		inlineAttachments.add(ia);
		return ia;
	}
	
	@Override
	public void setSubject(UTF8String subject) {
		if(subject != null) {
			this.subject = subject;
		}
	}

	@Override
	public void setBodyText(UTF8String bodyText) {
		if(bodyText != null) {
			this.bodyText = bodyText;
		}
	}

	@Override
	public Collection<UTF8String> getTOList() {
		return toList;
	}

	@Override
	public Collection<UTF8String> getCCList() {
		return ccList;
	}

	@Override
	public Collection<UTF8String> getBCCList() {
		return bccList;
	}

	@Override
	public UTF8String getSubject() {
		return subject;
	}

	@Override
	public UTF8String getBodyText() {
		return bodyText;
	}

	@Override
	public Collection<EmailAttachment> getAttachments() {
		return attachments;
	}

	@Override
	public Collection<EmailAttachment> getInlineAttachments() {
		return inlineAttachments;
	}
}
