package com.bundee.msfw.servicefw.srvutils.monitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.logi.BLogger;

public class MonitoringTracker {
	private List<PerfMonData> perfMonDataList;
	private Set<SvcMonData> svcMonDataList;
	private String eventID;
	private String sessionID;
	private boolean bPerfMonEnabled;
	private boolean bSvcMonEnabled;

	public MonitoringTracker(boolean bPerfMonEnabled, boolean bSvcMonEnabled, String eventID, String sessionID) {
		perfMonDataList = new ArrayList<PerfMonData>();
		svcMonDataList = new HashSet<SvcMonData>();
		this.eventID = eventID;
		this.sessionID = sessionID;
		this.bPerfMonEnabled = bPerfMonEnabled;
		this.bSvcMonEnabled = bSvcMonEnabled;
	}

	public PerfMonData startTracking(MonEvent evt, String trackedName) {
		PerfMonData perfData = new PerfMonData(eventID, sessionID, evt, trackedName);
		if (bPerfMonEnabled) {
			perfMonDataList.add(perfData);
		}
		return perfData;
	}

	public void endTracking(PerfMonData perfData, Throwable ex) {
		if (perfData == null || !bPerfMonEnabled) {
			return;
		}
		if (ex != null) {
			if (ex instanceof BExceptions) {
				BExceptions pe = (BExceptions) ex;
				perfData.setErrCode(pe.getCode().getCode());
			} else if (ex instanceof DBException) {
				DBException de = (DBException) ex;
				perfData.setErrCode(de.getSqlError());
			} else {
				perfData.setErrCode(-1);
			}
		}
		perfData.conclude();
	}

	public void endTracking(PerfMonData perfData) {
		endTracking(perfData, null);
	}

	public void addSvcMonData(SvcMonData svcMonData) {
		if (svcMonData != null && bSvcMonEnabled) {
			svcMonDataList.add(svcMonData);
		}
	}

	public void fillSvcMondata(Set<SvcMonData> svcMonDataList) {
		svcMonDataList.addAll(this.svcMonDataList);
	}
	
	public void finalizePM(BLogger logger, String thisHostName, DBManager dbm) {
		if (bPerfMonEnabled) {
			logPM(logger);
			/*
			if (dbm != null) {
				logPM(logger);
				PerfMonDataDAO perfMonDataDAO = new PerfMonDataDAO(dbm, thisHostName);
				perfMonDataDAO.persistPerfMonData(logger, perfMonDataList);
			}
			*/
		}
	}

	public void finalizeSM(BLogger logger, String thisHostName, DBManager dbm) {
		if (dbm != null && bSvcMonEnabled) {
			logSM(logger);
			/*
			PerfMonDataDAO perfMonDataDAO = new PerfMonDataDAO(dbm, thisHostName);
			perfMonDataDAO.persistSvcMonData(logger, svcMonDataList);
			*/
		}
	}

	public void logPM(BLogger logger) {
		perfMonDataList.forEach(pd -> {
			pd.log(logger);
		});
	}

	public void logSM(BLogger logger) {
		svcMonDataList.forEach(smd -> {
			smd.log(logger);
		});
	}

	public String getEventID() {
		return this.eventID;
	}

	public String getSessionID() {
		return this.sessionID;
	}

}
