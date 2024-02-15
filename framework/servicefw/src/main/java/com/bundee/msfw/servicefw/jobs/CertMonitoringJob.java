package com.bundee.msfw.servicefw.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.net.ssl.TrustManagerFactory;

import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import org.apache.commons.io.FilenameUtils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.RecurringJob;
import com.bundee.msfw.interfaces.emaili.EmailMessage;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.fcfgi.Application;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.UtilFactory;
import com.bundee.msfw.interfaces.utili.html.DynamicRow;
import com.bundee.msfw.interfaces.utili.html.DynamicRowSet;
import com.bundee.msfw.interfaces.utili.html.HTMLBuilder;
import com.bundee.msfw.servicefw.srvutils.config.CommonFileCfgDefs;

public class CertMonitoringJob implements RecurringJob {
	private final String SVC_MON_TEMPL_FN = "cert_expiry_reminder.html";
	private final String HOST_NAME_KEY = "HOST_NAME";
	private final String APP_NAME_KEY = "APP_NAME";
	private final String CERT_MON_TABLE_KEY = "certmon";
	
	String certPath;
	Long cmnExpiryNoticePeriod;
	Long cmnReminderFreq;

	Set<String> processedFiles = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	Map<String, CertDetails> certificates = new TreeMap<String, CertDetails>(String.CASE_INSENSITIVE_ORDER);
	Set<UTF8String> toList = null;
	Set<UTF8String> ccList = null;

	private class CertDetails {
		private Set<String> aliases = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		private String serialNumber;
		private String subject;
		private String issuer;
		private Set<String> sans;
		private Date notBeforeDate;
		private Date notAfterDate;
		
		private int daysLeft = 0;
		private long notBeforeSecs = 0;
		private long notAfterSecs = 0;
		private long lastNotificationTS = 0;
		
		public CertDetails(String alias, X509Certificate cert) {
			aliases.add(alias);
			subject = cert.getSubjectX500Principal().getName();
			issuer = cert.getIssuerX500Principal().getName();
			notAfterDate = cert.getNotAfter();
			notBeforeDate = cert.getNotBefore();
			sans = getSANHostNames(cert);
			
			notAfterSecs = notAfterDate.getTime()/1000;
			notBeforeSecs = notBeforeDate.getTime()/1000;
		}
		
		public void addAlias(String alias) {
			aliases.add(alias);
		}
		
		public void log(BLogger logger) {
			logger.debug("aliases: [" + aliases.toString() + "] subj: [" + subject + "] isser: [" + issuer
			+ "] cd: [" + notBeforeDate.toString() + "] ed: [" + notAfterDate.toString() + "] sn: [" + serialNumber
			+ "] sans: [" + sans.toString() + "]");

		}
	}
	public CertMonitoringJob(String certPath) {
		this.certPath = certPath;
	}

	public void setReminderFreq(Long cmnReminderFreq) {
		this.cmnReminderFreq = cmnReminderFreq;
	}

	public void setExpiryNoticePeriod(Long cmnExpiryNoticePeriod) {
		this.cmnExpiryNoticePeriod = cmnExpiryNoticePeriod;
	}

	@Override
	public String getJobType() {
		return "cert-check";
	}

	@Override
	public boolean useDistributor() {
		return false;
	}

	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
		File certDir = new File(certPath);
		if (!certDir.isDirectory()) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, certPath + " is not a folder! CertMonitoringJob will not function.");
		}

		String toListStr = blModServices.getFileCfgHandler().getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_NOTIFICATION_TO_LIST.getValue());
		String ccListStr = blModServices.getFileCfgHandler().getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_NOTIFICATION_CC_LIST.getValue());
		
		toList = new HashSet<UTF8String>();
		ccList = new HashSet<UTF8String>();
		
		fillList(toListStr, toList);
		fillList(ccListStr, ccList);
		
		if(toList.isEmpty() && ccList.isEmpty()) {
			throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "Either " + CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_NOTIFICATION_TO_LIST.getValue() + " or " + CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_NOTIFICATION_CC_LIST.getValue() + " must be configured!");
		}
	}

	@Override
	public void execute(BLogger logger, BLModServices blModServices) throws BExceptions {
		logger.debug("CertMonitoringJob: starting job run!");
		File certDir = new File(certPath);
		String[] files = certDir.list();
		BExceptions exceptions = new BExceptions();

		for (String file : files) {
			File filef = new File(file);
			if (filef.isDirectory())
				continue;
			String ksPath = certPath + file;
			String type = getKSType(ksPath);
			if (type != null) {
				if (!processedFiles.contains(ksPath)) {
					processedFiles.add(ksPath);
					collectCertificateDetails(logger, ksPath, type, exceptions);
				}
			}
		}
		
		Map<String, CertDetails> certificates4Notification = getCertsTobeNotified(logger);
		sendNotification(logger, blModServices, exceptions, certificates4Notification);
		
		if (exceptions.hasExceptions()) {
			throw exceptions;
		}

		logger.debug("CertMonitoringJob: job run ended!");
	}

	private Map<String, CertDetails> getCertsTobeNotified(BLogger logger) {
		long currentTimeSecs = System.currentTimeMillis()/1000;
		Map<String, CertDetails> certificates4Notification = new TreeMap<String, CertDetails>(String.CASE_INSENSITIVE_ORDER);
		
		for(Map.Entry<String, CertDetails> pair : certificates.entrySet()) {
			String sn = pair.getKey();
			CertDetails certDetails = pair.getValue();
			
			long timeLeft = (certDetails.notAfterSecs > currentTimeSecs ? certDetails.notAfterSecs - currentTimeSecs : 0); 
			//Its in EXPIRY NOTICE period
			if(timeLeft <= cmnExpiryNoticePeriod) {
				//We need to remind again
				if(certDetails.lastNotificationTS <= 0 || cmnReminderFreq <= (currentTimeSecs-certDetails.lastNotificationTS)) {
					certDetails.lastNotificationTS = currentTimeSecs;
					certDetails.daysLeft = convertSecs2Days(timeLeft);
					certificates4Notification.put(sn, certDetails);
				}
			} else if(currentTimeSecs < certDetails.notBeforeSecs) {
				//A very rare case where a certificate is deployed before it's acceptance is started
			}
		}
		
		return certificates4Notification;
	}

	private void sendNotification(BLogger logger, BLModServices blModServices, BExceptions exceptions, Map<String, CertDetails> certificates4Notification) throws BExceptions {
		if(certificates4Notification.isEmpty()) return;
		
		Application appDetails = blModServices.getFileCfgHandler().getApplication();
		UtilFactory utilFactory = blModServices.getUtilFactory();
		
		String htmlBody = buildHTML(logger, appDetails, utilFactory);
		EmailerService emailerService = blModServices.getEmailerService();
		EmailMessage em = emailerService.getNewEmailMessage();
		em.setBodyText(new UTF8String(htmlBody));

		em.setSubject(new UTF8String(String.format("Certificates Expiring Soon")));
		
		addList2Message(true, toList, em);
		addList2Message(false, ccList, em);
		emailerService.sendEmail(logger, em);
	}
	
	private String buildHTML(BLogger logger, Application appDetails, UtilFactory utilFactory) throws BExceptions {
		HTMLBuilder htmlBuilder = utilFactory.getHTMLBuilder(SVC_MON_TEMPL_FN);
		htmlBuilder.setValue(HOST_NAME_KEY, appDetails.getThisHost());
		htmlBuilder.setValue(APP_NAME_KEY, appDetails.getName());
		
		DynamicRowSet drs = htmlBuilder.addDynamicRowSet(CERT_MON_TABLE_KEY);
		
		for(Map.Entry<String, CertDetails> pair : certificates.entrySet()) {
			CertDetails cd = pair.getValue();
			DynamicRow dr = drs.addRow("#ffffff");
			
			dr.addColumn(cd.subject);
			dr.addColumn(Integer.toString(cd.daysLeft));
			
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));

			dr.addColumn(df.format(cd.notAfterDate));
			
			dr.addColumn(cd.issuer);
			dr.addColumn(cd.sans.isEmpty() ? "" : cd.sans.toString());
		}
		
		return htmlBuilder.build(logger);
	}
	
	private void collectCertificateDetails(BLogger logger, String ksPath, String type, BExceptions exceptions) {
		logger.debug("Logging details for " + ksPath);

		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance(type);
			ks.load(new FileInputStream(ksPath), null);
			trustManagerFactory.init(ks);

			Enumeration<String> aliases = ks.aliases();
			Iterator<String> itr = aliases.asIterator();
			while (itr.hasNext()) {
				String alias = itr.next();

				X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
				if(cert != null) {
				String sn = cert.getSerialNumber().toString();
				
				CertDetails certDetails = null;
				if(certificates.containsKey(sn)) {
					certDetails = certificates.get(sn);
					certDetails.addAlias(alias); 
					continue;
				}
				
				certDetails = new CertDetails(alias, cert);
				certificates.put(sn, certDetails);
				
				certDetails.log(logger);
				
				} else {
					logger.debug("alias: [" + alias + "] has no certificate entries!");
				}
			}
		} catch (Exception e) {
			exceptions.add(e, FwConstants.PCodes.INTERNAL_ERROR);
		}
	}

	private static String getKSType(String filePath) {
		String type = null;
		String fext = FilenameUtils.getExtension(filePath);
		if (fext.equalsIgnoreCase("p12")) {
			type = "PKCS12";
		} else if (fext.equalsIgnoreCase("jks")) {
			type = "JKS";
		}

		return type;
	}
	
	private static Set<String> getSANHostNames(X509Certificate cert) {
	    Set<String> hostNameList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	    try {
	        Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
	        if (altNames != null) {
	            for(List<?> altName : altNames) {
	                if(altName.size()< 2) continue;
	                /*
	                switch((Integer)altName.get(0)) {
	                    case GeneralName.dNSName:
	                    case GeneralName.iPAddress:
	                        Object data = altName.get(1);
	                        if (data instanceof String) {
	                            hostNameList.add(((String)data));
	                        }
	                        break;
	                    default:
	                }
	                */
	            }
	        }
	    } catch(CertificateParsingException e) {
	    }
	    return hostNameList;
	}

	private void fillList(String listStr, Set<UTF8String> listObj) {
		if(listStr == null || listStr.isBlank()) return;
		String[] listVals = listStr.split(",");
		for(String lv : listVals) {
			listObj.add(new UTF8String(lv));
		}
	}

	private void addList2Message(boolean bTOList, Set<UTF8String> list, EmailMessage em) {
		for(UTF8String lv : list) {
			if(bTOList) {
				em.add2TOList(lv);
			} else {
				em.add2CCList(lv);
			}
		}
	}
	
	private static int convertSecs2Days(long timeLeftSecs) {
		return (int)(timeLeftSecs/(60 * 60 * 24));
	}
}
