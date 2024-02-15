package com.bundee.msfw.servicefw.jobs;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringHelper;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;
import com.bundee.msfw.servicefw.srvutils.monitor.SvcMonData;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class SvcMonNotificationJob implements RecurringJob {
	private final String SVC_MON_TEMPL_FN = "svc_mon_metrics.html";
	private final String HOST_NAME_KEY = "HOST_NAME";
	private final String APP_NAME_KEY = "APP_NAME";
	private final String FROM_TIME_KEY = "FROM_TIME";
	private final String TO_TIME_KEY = "TO_TIME";
	private final String SVC_MON_TABLE_KEY = "svcmon";

	boolean bNotificationOnFailureOnly = true;
	List<MonitoringTracker> lastNMonEntries;
	Long lastNotificationTS = 0L;
	Set<UTF8String> toList = null;
	Set<UTF8String> ccList = null;

	public SvcMonNotificationJob() {
		lastNMonEntries = new ArrayList<MonitoringTracker>();
	}

	@Override
	public String getJobType() {
		return "svc-mon-notification";
	}

	@Override
	public boolean useDistributor() {
		return false;
	}

	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
		String toListStr = blModServices.getFileCfgHandler()
				.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_NOTIFICATION_TO_LIST.getValue());
		String ccListStr = blModServices.getFileCfgHandler()
				.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_NOTIFICATION_CC_LIST.getValue());

		String ntfnOnFailureOnlyStr = blModServices.getFileCfgHandler()
				.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_NOTIFICATION_ON_FAILURE_ONLY.getValue());
		if (ntfnOnFailureOnlyStr != null && !ntfnOnFailureOnlyStr.isBlank()) {
			bNotificationOnFailureOnly = Boolean.parseBoolean(ntfnOnFailureOnlyStr);
		}

		lastNotificationTS = System.currentTimeMillis();

		toList = new HashSet<UTF8String>();
		ccList = new HashSet<UTF8String>();

		fillList(toListStr, toList);
		fillList(ccListStr, ccList);

		if (toList.isEmpty() && ccList.isEmpty()) {
			throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
					"Either " + CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_NOTIFICATION_TO_LIST.getValue() + " or "
							+ CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_NOTIFICATION_CC_LIST.getValue()
							+ " must be configured!");
		}
	}

	@Override
	public void execute(BLogger logger, BLModServices blModServices) throws BExceptions {
		MonitoringTracker lastMonData = MonitoringHelper.getLastMonTrack();
		if (lastMonData == null) {
			throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "Service monitoring is not enabled!");
		}
		lastNMonEntries.add(lastMonData);

		Long curTS = System.currentTimeMillis();
		sendNotification(logger, blModServices, curTS, lastNotificationTS);
		lastNMonEntries.clear();
		lastNotificationTS = curTS;
	}

	private void sendNotification(BLogger logger, BLModServices blModServices, Long toTimeMS, Long fromTimeMS)
			throws BExceptions {
		Map<String, MonStats> svcMonStats = new TreeMap<String, MonStats>(String.CASE_INSENSITIVE_ORDER);
		int failedServices = extractMonStats(logger, svcMonStats);

		if (bNotificationOnFailureOnly && failedServices <= 0) {
			logger.debug("SvcMonNotificationJob: Email notification will not be sent, there are no failed services!");
			return;
		}

		Application appDetails = blModServices.getFileCfgHandler().getApplication();
		UtilFactory utilFactory = blModServices.getUtilFactory();

		String htmlBody = buildHTML(logger, appDetails, utilFactory, svcMonStats, toTimeMS, fromTimeMS);
		EmailerService emailerService = blModServices.getEmailerService();
		EmailMessage em = emailerService.getNewEmailMessage();
		em.setBodyText(new UTF8String(htmlBody));

		String hostApp = String.format("%s : %s", appDetails.getThisHost(), appDetails.getName());
		em.setSubject(new UTF8String(String.format("Service Monitoring Metrics - %s", hostApp)));

		addList2Message(true, toList, em);
		addList2Message(false, ccList, em);
		emailerService.sendEmail(logger, em);
	}

	private void addList2Message(boolean bTOList, Set<UTF8String> list, EmailMessage em) {
		for (UTF8String lv : list) {
			if (bTOList) {
				em.add2TOList(lv);
			} else {
				em.add2CCList(lv);
			}
		}
	}

	private void fillList(String listStr, Set<UTF8String> listObj) {
		if (listStr == null || listStr.isBlank())
			return;
		String[] listVals = listStr.split(",");
		for (String lv : listVals) {
			listObj.add(new UTF8String(lv));
		}
	}

	private int extractMonStats(BLogger logger, Map<String, MonStats> svcMonStats) throws BExceptions {
		int failedServices = 0;
		for (MonitoringTracker tracker : lastNMonEntries) {
			Set<SvcMonData> svcMonDataList = new HashSet<SvcMonData>();
			tracker.fillSvcMondata(svcMonDataList);
			for (SvcMonData smond : svcMonDataList) {
				String svcName = smond.getServiceName();
				MonStats ms = null;
				if (svcMonStats.containsKey(svcName)) {
					ms = svcMonStats.get(svcName);
				} else {
					ms = new MonStats();
					svcMonStats.put(svcName, ms);
				}

				ms.totalMonAttempts++;

				if (smond.getErrCode() != 0) {
					failedServices++;
					ms.totalMonFailure++;
					ms.errorDetails.add(smond.getErrDetail());
				}
			}
		}

		return failedServices;
	}

	private String buildHTML(BLogger logger, Application appDetails, UtilFactory utilFactory,
			Map<String, MonStats> svcMonStats, Long toTimeMS, Long fromTimeMS) throws BExceptions {
		HTMLBuilder htmlBuilder = utilFactory.getHTMLBuilder(SVC_MON_TEMPL_FN);
		htmlBuilder.setValue(HOST_NAME_KEY, appDetails.getThisHost());
		htmlBuilder.setValue(APP_NAME_KEY, appDetails.getName());
		htmlBuilder.setValue(FROM_TIME_KEY, formatTime(fromTimeMS));
		htmlBuilder.setValue(TO_TIME_KEY, formatTime(toTimeMS));

		DynamicRowSet drs = htmlBuilder.addDynamicRowSet(SVC_MON_TABLE_KEY);
		buildRows(logger, svcMonStats, drs);

		return htmlBuilder.build(logger);
	}

	private void buildRows(BLogger logger, Map<String, MonStats> svcMonStats, DynamicRowSet drs) {
		for (Map.Entry<String, MonStats> pair : svcMonStats.entrySet()) {
			String svcName = pair.getKey();
			MonStats ms = pair.getValue();
			String rowColor = "#90EE90";
			if (ms.totalMonAttempts > 0 && ms.totalMonFailure > 0) {
				double tmf = ms.totalMonFailure;
				double tma = ms.totalMonAttempts;
				double factor = tmf / tma;
				if (factor >= 0.2) {
					rowColor = "#FFCCCB";
				} else if (factor >= 0.1) {
					rowColor = "#FFFFE0";
				}
			}

			// Build row
			DynamicRow dr = drs.addRow(rowColor);

			dr.addColumn(svcName);
			dr.addColumn(Integer.toString(ms.totalMonFailure) + "/" + Integer.toString(ms.totalMonAttempts));
			StringBuffer buffer = new StringBuffer();
			for (String ed : ms.errorDetails) {
				buffer.append(ed).append("\n");
			}
			dr.addColumn(buffer.toString());
		}
	}

	public String formatTime(long time) {
		Date date = new Date(time);
		Format format = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss");
		return format.format(date);
	}

	private class MonStats {
		public int totalMonAttempts = 0;
		public int totalMonFailure = 0;
		public Set<String> errorDetails = new HashSet<String>();
	}
}
