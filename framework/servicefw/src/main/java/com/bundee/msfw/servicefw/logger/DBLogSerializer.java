package com.bundee.msfw.servicefw.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.servicefw.dbm.DBManagerFactory;

public class DBLogSerializer {
	DBManager dbm;
	ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	Map<Long, List<LogRecord>> threadLogRecords = new HashMap<Long, List<LogRecord>>();
	int logSecs;

	public DBLogSerializer(DBManagerFactory dbmf, int logSecs, boolean bOnlyEvent) throws BExceptions {
		this.dbm = dbmf.getDBM("log");
		this.logSecs = logSecs;

		if (!bOnlyEvent) {
			LogPersistor lp = new LogPersistor(this);
			Timer timer = new Timer(true);
			timer.scheduleAtFixedRate(lp, logSecs * 1000, logSecs * 1000);
		}
	}

	private class LogRecord {
		public String server;
		public long threadID;
		public String logLevel;
		public String mn;
		public String apiName;
		public String msg;
	}

	public void insertLog(String server, long threadID, String logLevel, String mn, String apiName, String msg) {
		LogRecord lr = new LogRecord();
		lr.server = server;
		lr.threadID = threadID;
		lr.logLevel = logLevel;
		lr.mn = mn;
		lr.apiName = apiName;
		lr.msg = msg;

		ReadLock rl = rwLock.readLock();
		rl.lock();
		List<LogRecord> recs = null;
		if (threadLogRecords.containsKey(threadID)) {
			recs = threadLogRecords.get(threadID);
		} else {
			recs = new ArrayList<LogRecord>();
			threadLogRecords.put(threadID, recs);
		}
		recs.add(lr);
		rl.unlock();
	}

	public void insertLogNow(String server, long threadID, String logLevel, String mn, String apiName, String msg) {
		LogRecord lr = new LogRecord();
		lr.server = server;
		lr.threadID = threadID;
		lr.logLevel = logLevel;
		lr.mn = mn;
		lr.apiName = apiName;
		lr.msg = msg;

		persistOneLog(lr);
	}

	private static final String INSERT_LOG = "insert into log_table (server, thread_id, log_level, mod_nm, api_nm, log_msg) values (?, ?, ?, ?, ?, ?)";

	public void persistOneLog(LogRecord lr) {
		if (lr == null)
			return;

		DBQuery iq = dbm.getDBQueryBuilder().setQueryString(INSERT_LOG).throwOnNoData(false)
				.setBindInputFunction((logger, ps) -> {
					ps.setString(1, lr.server);
					ps.setLong(2, lr.threadID);
					ps.setString(3, lr.logLevel);
					ps.setString(4, lr.mn);
					ps.setString(5, lr.apiName);
					ps.setString(6, lr.msg);
					ps.addBatch();
				}).build();
		try {
			dbm.update(BLoggerFactory.getDummyLogger(), iq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void persistLogs(Map<Long, List<LogRecord>> threadLogRecords) {
		if (threadLogRecords.isEmpty())
			return;

		DBQuery iq = dbm.getDBQueryBuilder().setQueryString(INSERT_LOG).throwOnNoData(false).setBatch()
				.setBindInputFunction((logger, ps) -> {
					for (Map.Entry<Long, List<LogRecord>> pair : threadLogRecords.entrySet()) {
						for (LogRecord lr : pair.getValue()) {
							ps.setString(1, lr.server);
							ps.setLong(2, lr.threadID);
							ps.setString(3, lr.logLevel);
							ps.setString(4, lr.mn);
							ps.setString(5, lr.apiName);
							ps.setString(6, lr.msg);
							ps.addBatch();
						}
					}
				}).build();
		try {
			dbm.update(BLoggerFactory.getDummyLogger(), iq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class LogPersistor extends TimerTask {
		DBLogSerializer dbls;

		LogPersistor(DBLogSerializer dbls) {
			this.dbls = dbls;
		}

		@Override
		public void run() {
			System.out.println("LogPersistor:run in " + System.currentTimeMillis());

			WriteLock wl = rwLock.writeLock();
			wl.lock();
			Map<Long, List<LogRecord>> tlr = dbls.threadLogRecords;
			dbls.threadLogRecords = new HashMap<Long, List<LogRecord>>();
			wl.unlock();

			persistLogs(tlr);
			System.out.println("LogPersistor:run out " + System.currentTimeMillis());
		}
	}
}
