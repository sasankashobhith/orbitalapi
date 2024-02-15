package com.bundee.msfw.servicefw.srvutils.email;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.defs.ProcessingCode;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.emaili.EmailAttachment;
import com.bundee.msfw.interfaces.emaili.EmailMessage;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import com.bundee.msfw.servicefw.srvutils.utils.ServiceIniter;

public class EmailerServiceImpl implements EmailerService, ServiceIniter {
	private static final String IMG_PFX = "##IMG_PATH_";
	private static final String IMG_SFX = "##";
	
	Properties properties;

	String smtpHost;
	String smtpPort;
	boolean bDebugON;
	String senderEmail;

	@Override
	public void init(BLogger logger, FileCfgHandler fch, VaultService vaultService, BLModServices blModServices)
			throws BExceptions {
		smtpHost = blModServices.getFileCfgHandler().getCfgParamStr("email.smtp.host");
		smtpPort = blModServices.getFileCfgHandler().getCfgParamStr("email.smtp.port");

		senderEmail = blModServices.getFileCfgHandler().getCfgParamStr("email.smtp.sender.email");
		String strDebug = blModServices.getFileCfgHandler().getCfgParamStr("email.smtp.debug");
		bDebugON = Boolean.parseBoolean(strDebug);

		properties = new Properties();
		// Setup mail server
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.setProperty("mail.smtp.auth", "false");

		properties.put("mail.smtp.socketFactory.port", smtpPort);
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		if (bDebugON) {
			properties.put("mail.debug", "true");
		} else {
			properties.put("mail.debug", "false");
		}
	}

	@Override
	public void sendEmail(BLogger logger, EmailMessage message) throws BExceptions {

		logger.info("Binding email message");
		try {
			Session session = Session.getDefaultInstance(properties);

			if (bDebugON) {
				session.setDebug(true);
				session.setDebugOut(logger.getPS());
			}

			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress(senderEmail));

			if (message.getTOList() == null || message.getTOList().isEmpty()) {
				throw new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Mandatory field missing - To List");
			} else {
				for (UTF8String to : message.getTOList()) {
					mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to.getUTF8String()));
				}
			}
			if (message.getCCList() != null && !message.getCCList().isEmpty()) {
				for (UTF8String cc : message.getCCList()) {
					mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc.getUTF8String()));
				}
			}
			if (message.getBCCList() != null && !message.getBCCList().isEmpty()) {
				for (UTF8String cc : message.getBCCList()) {
					mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(cc.getUTF8String()));
				}
			}
			if (message.getSubject() == null || message.getSubject().isEmpty()) {
				throw new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Mandatory field missing - Subject");
			} else {
				mimeMessage.setSubject(message.getSubject().getUTF8String());
			}

			if (message.getBodyText() == null || message.getBodyText().isEmpty()) {
				throw new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Mandatory field missing - Body");
			}

			UTF8String bodyText = new UTF8String("");
			BodyPart messageBodyPart = new MimeBodyPart();
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			if(message.getBodyText() != null && message.getBodyText().getUTF8String() != null) {
				String pbt = processImages(logger, multipart, message.getBodyText().getUTF8String());
				bodyText = new UTF8String(pbt);
			} else {
				bodyText = new UTF8String("");
			}
			
			messageBodyPart.setContent(bodyText.getUTF8String(), "text/html; charset=utf-8");

			for (EmailAttachment attachment : message.getAttachments()) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				ByteArrayDataSource source = new ByteArrayDataSource(attachment.getContents(),
						attachment.getMIME().getUTF8String());
				attachmentPart.setDataHandler(new DataHandler(source));
				attachmentPart.setFileName(attachment.getName().getUTF8String());
				multipart.addBodyPart(attachmentPart);
			}
            for (EmailAttachment attachment : message.getInlineAttachments()) {
                MimeBodyPart inlineAttachmentPart = new MimeBodyPart();
                ByteArrayDataSource source = new ByteArrayDataSource(attachment.getContents(),
                        attachment.getMIME().getUTF8String());
                inlineAttachmentPart.setDataHandler(new DataHandler(source));
                inlineAttachmentPart.setDisposition(MimeBodyPart.INLINE);
                inlineAttachmentPart.setContentID(attachment.getName().getUTF8String());
                multipart.addBodyPart(inlineAttachmentPart);
            }			
			mimeMessage.setContent(multipart);
			Transport.send(mimeMessage);
			logger.info("Sent email message successfully....");
		} catch (MessagingException me) {
			logger.warn("Message Exception in transporting SLA alert email: " + me.getMessage());
			if (me instanceof SendFailedException) {
				Address[] invalidAddrs = ((SendFailedException) me).getInvalidAddresses();
				if (invalidAddrs != null && invalidAddrs.length > 0) {
					for (Address addr : invalidAddrs) {
						logger.warn("invalid address: " + addr.toString());
					}
				}
			}
			throw new BExceptions(me, FwConstants.PCodes.INVALID_VALUE);
		} catch (Exception mex) {
			logger.error(mex);
			throw new BExceptions(mex, FwConstants.PCodes.INVALID_VALUE);
		}
	}

	@Override
	public EmailMessage getNewEmailMessage() {
		return new EmailMessageImpl();
	}

	@Override
	public HealthDetails checkHealth(BLogger logger) {
		HealthDetails hd = new HealthDetails();
		Session session = Session.getDefaultInstance(properties);
		try {
			session.getTransport().connect();
			hd.add(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY, smtpHost + ":" + smtpPort);
		} catch (MessagingException e) {
			hd.add(FwConstants.PCodes.EMAILER_SERVICE_FAILURE, e.getMessage(), smtpHost + ":" + smtpPort);
		}

		return hd;
	}

	private String processImages(BLogger logger, Multipart multipart, String bodyText) {
		String processedBT = bodyText;
		if(bodyText == null || bodyText.isBlank()) return processedBT;
		
		int idx = -1;
		while((idx = processedBT.indexOf(IMG_PFX)) >= 0) {
			try {
				idx += IMG_PFX.length();
				int sidx = processedBT.indexOf(IMG_SFX, idx);
				String imgFN = processedBT.substring(idx, sidx);
				//TODO: String absImgFN = ServiceFramework.getInstance().getApplication().getImagesFolder() + File.separator + imgFN;
				String absImgFN = "";
				File imageFile = new File(absImgFN);
				URL imageURL = imageFile.toURI().toURL();
				
				String cid = UUID.randomUUID().toString();
				MimeBodyPart imgAttachmentPart = new MimeBodyPart();
				imgAttachmentPart.setDataHandler(new DataHandler(imageURL));
				imgAttachmentPart.setDisposition(MimeBodyPart.INLINE);
				imgAttachmentPart.setContentID("<" + cid + ">");
				multipart.addBodyPart(imgAttachmentPart);
				
				String imageStr = IMG_PFX + imgFN + IMG_SFX;
				processedBT = processedBT.replaceAll(imageStr, "\"cid:" + cid + "\"");
			} catch (MessagingException | MalformedURLException e) {
				logger.error(e);
			}
		}
		
		return processedBT;
	}
}
