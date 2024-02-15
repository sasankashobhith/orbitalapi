package com.bundee.msfw.servicefw.srvutils.monitor;

import java.util.List;

import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.logi.BLogger;

public class DBMonitor implements DBManager {

	DBManager dbm;
	MonitoringTracker monTracker;
	
	public DBMonitor(DBManager dbm, MonitoringTracker monTracker) {
		this.dbm = dbm;
		this.monTracker = monTracker;
	}
	
	@Override
	public DBQueryBuilder getDBQueryBuilder() {
		return dbm.getDBQueryBuilder();
	}

	@Override
	public boolean update(BLogger logger, DBQuery query) throws DBException {
		PerfMonData perfData = monTracker.startTracking(MonEvent.DB, "DBManager::update");
		boolean val = false;
		try {
			val = dbm.update(logger, query);
			monTracker.endTracking(perfData);
		} catch (DBException ex) {
			monTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public boolean update(BLogger logger, List<DBQuery> queries) throws DBException {
		PerfMonData perfData = monTracker.startTracking(MonEvent.DB, "DBManager::updateMultiple");
		boolean val = false;
		try {
			val= dbm.update(logger, queries);
			monTracker.endTracking(perfData);
		} catch (DBException ex) {
			monTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public boolean select(BLogger logger, DBQuery query) throws DBException {
		PerfMonData perfData = monTracker.startTracking(MonEvent.DB, "DBManager::select");
		boolean val = false;
		try {
			val= dbm.select(logger, query);
			monTracker.endTracking(perfData);
		} catch (DBException ex) {
			monTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public boolean executeSP(BLogger logger, DBQuery query) throws DBException {
		PerfMonData perfData = monTracker.startTracking(MonEvent.DB, "DBManager::executeSP");
		boolean val = false;
		try {
			val= dbm.executeSP(logger, query);
			monTracker.endTracking(perfData);
		} catch (DBException ex) {
			monTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public HealthDetails checkHealth(BLogger logger) {
		return dbm.checkHealth(logger);
	}
}
