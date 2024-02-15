package com.bundee.msfw.servicefw.srvutils.monitor;

import java.util.List;
import java.util.Set;

import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.logi.BLogger;

public class PerfMonDataDAO {
	private static final String INSERT_PERF_DATA_SQL = "INSERT INTO PERF_MON_DATA (EVENTID, SESSIONID, EVENTTYPE, EVENTNAME, EVENTTS, PROCTIMEMS, ERRCODE, REQSIZE, RESPSIZE, SVCHOST) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_SVCMON_DATA_SQL = "INSERT INTO SVC_MON_DATA (EVENTID, SVCHOST, SVCTYPE, SVCNAME, EVENTTS, EXTHOST, ERRCODE, ERRDETAIL) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	
	DBManager dbm;
	String thisHostName;

	public PerfMonDataDAO(DBManager dbm, String thisHostName) {
		this.dbm = dbm;
		this.thisHostName = thisHostName;
	}

	public void persistPerfMonData(BLogger logger, List<PerfMonData> perfMonDataList) {
		if(perfMonDataList == null || perfMonDataList.isEmpty()) return;
		try {
			DBQuery insertPerfDataQ = dbm.getDBQueryBuilder().setQueryString(INSERT_PERF_DATA_SQL).setBatch()
					.logQuery(false)
					.setBindInputFunction((dbLogger, ps) -> {
						for (PerfMonData pd : perfMonDataList) {
							ps.setString(1, pd.getEventID());
							ps.setString(2, pd.getSessionID());
							ps.setShort(3, (short) pd.getEventType().getValue());
							ps.setString(4, pd.getEventName());
							ps.setLong(5, pd.getEventTMS());
							ps.setInt(6, pd.getProcTimeMS());
							ps.setInt(7, pd.getErrCode());
							ps.setInt(8, pd.getReqSize());
							ps.setInt(9, pd.getRespSize());
							ps.setString(10, thisHostName);
							ps.addBatch();
						}
					}).build();
			dbm.update(logger, insertPerfDataQ);
		} catch (DBException e) {
			logger.error(e);
		}
	}

	public void persistSvcMonData(BLogger logger, Set<SvcMonData> svcMonDataList) {
		if(svcMonDataList == null || svcMonDataList.isEmpty()) return;
		try {
			DBQuery insertsvcMonDataQ = dbm.getDBQueryBuilder().setQueryString(INSERT_SVCMON_DATA_SQL).setBatch()
					.logQuery(false)
					.setBindInputFunction((dbLogger, ps) -> {
						for (SvcMonData smd : svcMonDataList) {
							ps.setString(1, smd.getEventID());
							ps.setString(2, thisHostName);
							ps.setShort(3, (short) smd.getServiceType().getValue());
							ps.setString(4, smd.getServiceName());
							ps.setLong(5, smd.getEventTMS());
							ps.setString(6, smd.getExtHost());
							ps.setInt(7, smd.getErrCode());
							ps.setString(8, smd.getErrDetail());
							ps.addBatch();
						}
					}).build();
			dbm.update(logger, insertsvcMonDataQ);
		} catch (DBException e) {
			logger.error(e);
		}
	}
}
