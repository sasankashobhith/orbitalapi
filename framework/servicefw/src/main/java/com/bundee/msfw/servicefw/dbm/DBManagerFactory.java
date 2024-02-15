package com.bundee.msfw.servicefw.dbm;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import com.bundee.msfw.servicefw.logger.BLoggerFactory;
import com.bundee.msfw.servicefw.srvutils.monitor.MonEvent;
import com.bundee.msfw.servicefw.srvutils.monitor.PerfMonData;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.defs.ProcessingCode;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.vault.VaultService;

public class DBManagerFactory {
	static final String DB_SEP = ".";
	static final String DB_SEP_EXP = "\\.";
	static final String DB_PFX = "db" + DB_SEP;
	static final String ENABLED = "enabled";
	static final String DBN = "name";
	static final String HOST = "host";
	static final String PORT = "port";
	static final String USER = "user";
	static final String PWD = "pwd";
	static final String INIT_CONNS = "initConns";
	static final String MAX_CONNS = "maxConns";
	static final String THORW_ON_NODATA = "throwOnNoData";
	static final String ENABLE_SSL = "enableSSL";
	static final String MIN_EVIT_IDLE_TIME = "minEvictableIdleTimeSecs";
	static final String SOFT_MIN_EVIT_IDLE_TIME = "softMinEvictableIdleTimeSecs";
	static final String TEST_ON_BORROW = "testOnBorrow";

	static final String DB_DEF_DBM = "__default__";

	DBManagerImpl defaultDBM = null;
	Map<String, DBManager> dbManagerMap;
	String appName = "unk";

	public DBManager getDBM() {
		return defaultDBM;
	}

	public Map<String, DBManager> getAllDBManagers() {
		return dbManagerMap;
	}

	public DBManager getDBM(String dbCtx) throws BExceptions {
		DBManager dbm = null;
		if (dbCtx == null || dbCtx.isBlank()) {
			dbm = defaultDBM;
		} else if (dbManagerMap.containsKey(dbCtx)) {
			dbm = dbManagerMap.get(dbCtx);
		}

		if (dbm == null) {
			throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
					dbCtx + " this database is not configured!");
		}

		return dbm;
	}

	public void extractOneDBConfig(BLogger logger, String pfx, FileCfgHandler fcfgHandler, VaultService vault,
			Map<String, DBConnData> dbConnDataMap, BExceptions exceptions) {
		try {
			DBConnData dbConnData = new DBConnData();
			String dbenabled = (pfx != null ? DB_PFX + pfx + DB_SEP + ENABLED : DB_PFX + ENABLED);
			String dbnpn = (pfx != null ? DB_PFX + pfx + DB_SEP + DBN : DB_PFX + DBN);
			String dbhost = (pfx != null ? DB_PFX + pfx + DB_SEP + HOST : DB_PFX + HOST);
			String dbport = (pfx != null ? DB_PFX + pfx + DB_SEP + PORT : DB_PFX + PORT);
			String dbuser = (pfx != null ? DB_PFX + pfx + DB_SEP + USER : DB_PFX + USER);
			String dbpwd = (pfx != null ? DB_PFX + pfx + DB_SEP + PWD : DB_PFX + PWD);
			String dbinitconns = (pfx != null ? DB_PFX + pfx + DB_SEP + INIT_CONNS : DB_PFX + INIT_CONNS);
			String dbmaxconns = (pfx != null ? DB_PFX + pfx + DB_SEP + MAX_CONNS : DB_PFX + MAX_CONNS);
			String dbthrowonnodata = (pfx != null ? DB_PFX + pfx + DB_SEP + THORW_ON_NODATA : DB_PFX + THORW_ON_NODATA);
			String enablessl = (pfx != null ? DB_PFX + pfx + DB_SEP + ENABLE_SSL : DB_PFX + ENABLE_SSL);
			String minevictidlet = (pfx != null ? DB_PFX + pfx + DB_SEP + MIN_EVIT_IDLE_TIME
					: DB_PFX + MIN_EVIT_IDLE_TIME);
			String softminevictidlet = (pfx != null ? DB_PFX + pfx + DB_SEP + SOFT_MIN_EVIT_IDLE_TIME
					: DB_PFX + SOFT_MIN_EVIT_IDLE_TIME);
			// String testonborrow = (pfx != null ? DB_PFX + pfx + DB_SEP + TEST_ON_BORROW :
			// DB_PFX + TEST_ON_BORROW);

			int isDBEnabled = fcfgHandler.getCfgParamInt(dbenabled);
			if (isDBEnabled != 0) {
				dbConnData.dbName = fcfgHandler.getCfgParamStr(dbnpn);
				dbConnData.dbHost = fcfgHandler.getCfgParamStr(dbhost);
				dbConnData.dbUser = fcfgHandler.getCfgParamStr(dbuser);
				dbConnData.dbPort = fcfgHandler.getCfgParamInt(dbport);
				dbConnData.dbInitConns = fcfgHandler.getCfgParamInt(dbinitconns);
				dbConnData.dbMaxConns = fcfgHandler.getCfgParamInt(dbmaxconns);
				dbConnData.bThrowOnNoData = true;

				dbConnData.dbPwd = vault.getValue(logger, fcfgHandler.getCfgParamStr(dbpwd));
				if (fcfgHandler.getAllCfgParams().containsKey(dbthrowonnodata)) {
					int val = fcfgHandler.getCfgParamInt(dbthrowonnodata);
					dbConnData.bThrowOnNoData = (val == 0 ? false : true);
				}

				dbConnData.bEnableSSL = true;
				if (fcfgHandler.getAllCfgParams().containsKey(enablessl)) {
					dbConnData.bEnableSSL = Boolean.parseBoolean(fcfgHandler.getCfgParamStr(enablessl));
				}

				if (fcfgHandler.getAllCfgParams().containsKey(minevictidlet)) {
					dbConnData.minEvictableIdleTimeSecs = fcfgHandler.getCfgParamInt(softminevictidlet);
				}

				/*
				 * if (fcfgHandler.getAllCfgParams().containsKey(testonborrow)) {
				 * dbConnData.bTestOnBorrow =
				 * Boolean.parseBoolean(fcfgHandler.getCfgParamStr(testonborrow)); }
				 * 
				 * if (fcfgHandler.getAllCfgParams().containsKey(softminevictidlet)) {
				 * dbConnData.softMinEvictableIdleTimeSecs =
				 * fcfgHandler.getCfgParamInt(softminevictidlet); }
				 */

				String dbCtx = (pfx == null ? DB_DEF_DBM : pfx);
				dbConnData.validate(dbCtx);

				dbConnDataMap.put(dbCtx, dbConnData);
			}
		} catch (BExceptions e) {
			exceptions.add(e);
		} catch (Exception e) {
			exceptions.add(new BExceptions(e, FwConstants.PCodes.INVALID_VALUE));
		}
	}

	public void initializeDB(BLogger logger, FileCfgHandler fcfgHandler, VaultService vault) throws BExceptions {
		dbManagerMap = new TreeMap<String, DBManager>(String.CASE_INSENSITIVE_ORDER);
		BExceptions exceptions = new BExceptions();
		if (vault == null) {
			exceptions.add(new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
					"Vault service configuration is missing! Can't get DB password."));
			throw exceptions;
		}

		Set<String> dbPfxs = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Map<String, Object> dbConfigs = fcfgHandler.getAllCfgParams(DB_PFX);
		dbConfigs.entrySet().forEach(pair -> {
			String[] tokens = pair.getKey().split(DB_SEP_EXP);
			if (tokens != null && tokens.length == 3) {
				dbPfxs.add(tokens[1]);
			}
		});
		Map<String, DBConnData> dbConnDataMap = new TreeMap<String, DBConnData>(String.CASE_INSENSITIVE_ORDER);

		appName = fcfgHandler.getApplication().getName();
		extractOneDBConfig(logger, null, fcfgHandler, vault, dbConnDataMap, exceptions);
		dbPfxs.forEach(pfx -> {
			extractOneDBConfig(logger, pfx, fcfgHandler, vault, dbConnDataMap, exceptions);
		});

		if (exceptions.hasExceptions()) {
			throw exceptions;
		}

		for (Map.Entry<String, DBConnData> pair : dbConnDataMap.entrySet()) {
			try {
				String dbCtx = pair.getKey().toLowerCase();
				DBManagerImpl dbm = new DBManagerImpl();
				dbm.init(logger, pair.getValue());
				dbm.testDBWithQ(logger);
				dbManagerMap.put(dbCtx, dbm);
				if (dbCtx.equalsIgnoreCase(DB_DEF_DBM)) {
					defaultDBM = dbm;
				}
			} catch (BExceptions e) {
				exceptions.add(e);
			} catch (Exception e) {
				exceptions.add(new BExceptions(e, FwConstants.PCodes.CONFIGURATION_MISSING));
			}
		}

		if (exceptions.hasExceptions()) {
			throw exceptions;
		}
	}

	private class DBManagerImpl implements DBManager {
		DBPool _pool;
		String dbCtx = "";

		public boolean init(BLogger logger, DBConnData dbConnData) throws BExceptions {
			boolean bRes = false;
			try {
				logger.info("Initializing DB connection");
				dbCtx = dbConnData.dbHost + ":" + dbConnData.dbName;
				_pool = new DBPool(dbCtx, dbConnData);
				_pool.init(logger);
				logger.info(dbCtx + " Database initialized successfully!");
				bRes = true;
			} catch (Exception ex) {
				throw new BExceptions(ex, FwConstants.PCodes.CONFIGURATION_MISSING);
			}

			return bRes;
		}

		// --------
		private int validateResponse(int[] responses) {
			if (responses == null || responses.length == 0)
				return -1; // unknown success
			int ret = -1; // success
			for (int idx = 0; idx < responses.length; idx++) {
				int resp = responses[idx];
				if (resp == Statement.EXECUTE_FAILED) {
					ret = idx;
				}
			}
			return ret;
		}

		public boolean update(BLogger logger, DBQuery query) throws DBException {
			List<DBQuery> queries = new ArrayList<DBQuery>();
			queries.add(query);
			return update(logger, queries);
		}

		public boolean update(BLogger logger, List<DBQuery> queries) throws DBException {
			if (queries == null || queries.isEmpty()) {
				return false;
			}

			for (DBQuery q : queries) {
				if (q == null || q.getQS() == null) {
					throw new DBException(false, "Query is null!");
				}
			}

			boolean bRes = false;
			Connection conn = null;
			PreparedStatement[] statements = null;

			try {
				statements = new PreparedStatement[queries.size()];
				conn = _pool.getConnection(logger);
				conn.setAutoCommit(false);
				int idx = 0;
				Iterator<DBQuery> qitb = queries.iterator();
				while (qitb.hasNext()) {
					DBQuery query = qitb.next();
					PreparedStatement pstmt = null;

					if (query.returnKeys()) {
						pstmt = conn.prepareStatement(query.getQS(), Statement.RETURN_GENERATED_KEYS);
					} else {
						pstmt = conn.prepareStatement(query.getQS());
					}
					statements[idx] = pstmt;
					idx++;

					if (query.logQuery()) {
						logger.debug(pstmt.toString());
					}
				}

				if (idx == queries.size()) {
					idx = 0;
					Iterator<DBQuery> qite = queries.iterator();
					while (qite.hasNext()) {
						DBQuery query = qite.next();
						PreparedStatement pstmt = statements[idx];
						query.bindInput(logger, pstmt);
						boolean isBatch = query.isBatch();
						boolean bThrowOnNoData = _pool.bThrowOnNoData;
						if (query.throwOnNoData() != null) {
							bThrowOnNoData = query.throwOnNoData();
						}

						if (isBatch) {
							int[] responses = pstmt.executeBatch();
							int ret = validateResponse(responses);
							if (ret >= 0) {
								String msg = query.getQS() + " failed!";
								throw new DBException(true, msg);
							}
						} else {
							int uc = pstmt.executeUpdate();
							if (bThrowOnNoData && uc <= 0) {
								throw new DBException(true);
							} else {
								logger.debug("updated records: " + uc);
							}
						}
						query.setRowsUpdated(pstmt.getUpdateCount());
						if (query.returnKeys()) {
							ResultSet generatedKeys = pstmt.getGeneratedKeys();
							if (generatedKeys.next()) {
								query.fetchData(logger, generatedKeys);
								while (generatedKeys.next()) {
									query.fetchData(logger, generatedKeys);
								}
							} else {
								throw new DBException(true,
										"Keys were expected by the query but no keys were returned!");
							}
						}

						idx++;
					}
					conn.commit();
					conn.setAutoCommit(true);
					bRes = true;
				} else {
					rollback(conn, logger);
				}
			} catch (SQLException ex) {
				DBException dbx = new DBException(logger, ex);
				rollback(conn, logger);
				throw dbx;
			} catch (DBException ex) {
				rollback(conn, logger);
				throw ex;
			} finally {
				try {
					if (statements != null) {
						for (int idx = 0; idx < statements.length; idx++) {
							if (statements[idx] != null) {
								statements[idx].close();
							}
						}
					}
				} catch (SQLException e) {
				}
				_pool.releaseConnection(logger, conn);
			}

			return bRes;
		}

		private static void rollback(Connection conn, BLogger logger) {
			if(conn == null) return;
			
			try {
				conn.rollback();
				conn.setAutoCommit(true);
			} catch (Exception rex) {
				logger.warn("rollback failed");
				logger.error(rex);
			}

		}

		public boolean select(BLogger logger, DBQuery query) throws DBException {
			boolean bRes = false;
			Connection conn = null;
			PreparedStatement pstmt = null;
			boolean bThrowOnNoData = _pool.bThrowOnNoData;

			if (query == null || query.getQS() == null) {
				throw new DBException(false, "Query is null!");
			}

			if (query.logTrace()) {
				logger.debug("in select");
			}

			try {
				if (query.throwOnNoData() != null) {
					bThrowOnNoData = query.throwOnNoData();
				}

				if (query.logTrace()) {
					logger.debug("Before getConnection from pool");
				}
				conn = _pool.getConnection(logger);
				if (query.logTrace()) {
					logger.debug("After getConnection from pool");
				}

				String squery = query.getQS();
				pstmt = conn.prepareStatement(squery);

				query.bindInput(logger, pstmt);

				if (query.logQuery()) {
					logger.debug(pstmt.toString());
				}

				if (query.logTrace()) {
					logger.debug("Before executeQuery");
				}
				ResultSet rs = pstmt.executeQuery();
				if (query.logTrace()) {
					logger.debug("After executeQuery");
				}

				if (rs.isBeforeFirst()) {
					if (query.logTrace()) {
						logger.debug("Fetching data");
					}
					while (rs.next()) {
						query.fetchData(logger, rs);
					}
					if (query.logTrace()) {
						logger.debug("Completed Fetching data");
					}
					bRes = true;
				}
			} catch (SQLException ex) {
				throw new DBException(logger, ex);
			} finally {
				try {
					if (pstmt != null) {
						pstmt.close();
					}
				} catch (SQLException e) {
				}
				if (query.logTrace()) {
					logger.debug("Before releaseConnection");
				}
				_pool.releaseConnection(logger, conn);
				if (query.logTrace()) {
					logger.debug("After releaseConnection");
				}
			}

			if (bThrowOnNoData && bRes == false) {
				throw new DBException(true); // Row Not Found
			}
			return bRes;
		}

		public boolean executeSP(BLogger logger, DBQuery query) throws DBException {
			boolean bRes = false;
			Connection conn = null;
			CallableStatement cstmt = null;
			boolean bThrowOnNoData = _pool.bThrowOnNoData;

			if (query == null || query.getQS() == null)
				return false;

			try {
				if (query.throwOnNoData() != null) {
					bThrowOnNoData = query.throwOnNoData();
				}
				conn = _pool.getConnection(logger);
				String squery = query.getQS();
				cstmt = conn.prepareCall(squery);

				query.bindInput(logger, cstmt);
				query.registerOutParameterTypes(logger, cstmt);

				boolean bExecuted = cstmt.execute();
				if (bExecuted) {
					ResultSet rs = cstmt.getResultSet();
					if (rs.isBeforeFirst()) {
						while (rs.next()) {
							query.fetchData(logger, rs);
						}
						bRes = true;
					}
				}
			} catch (SQLException ex) {
				throw new DBException(logger, ex);
			} finally {
				try {
					if (cstmt != null) {
						cstmt.close();
					}
				} catch (SQLException e) {
				}
				_pool.releaseConnection(logger, conn);
			}

			if (bThrowOnNoData && bRes == false) {
				throw new DBException(true); // Row Not Found
			}
			return bRes;
		}

		@Override
		public DBQueryBuilder getDBQueryBuilder() {
			return new DBQueryBuilderImpl();
		}

		@Override
		public HealthDetails checkHealth(BLogger logger) {
			HealthDetails hd = new HealthDetails();
			try {
				testDBWithQ(logger);
				hd.add(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY, dbCtx);
			} catch (DBException e) {
				hd.add(new ProcessingCode(e.getSqlError(), "dbk"), e.getMessage(), dbCtx);
			}
			return hd;
		}

		private void testDBWithQ(BLogger logger) throws DBException {
			DBQuery dbq = getDBQueryBuilder().setQueryString("SELECT VERSION()").build();
			select(logger, dbq);
		}
	}

	private class DBConnData {
		public String dbName;
		public String dbHost;
		public String dbUser;
		public String dbPwd;
		public int dbPort;
		public int dbInitConns;
		public int dbMaxConns;
		public boolean bThrowOnNoData;
		public boolean bEnableSSL;
		public int minEvictableIdleTimeSecs = 120;

		void validate(String ctx) throws BExceptions {
			if (dbName == null || dbName.isBlank() || dbHost == null || dbHost.isBlank() || dbUser == null
					|| dbUser.isBlank() || dbPwd == null || dbPwd.isBlank() || dbPort <= 0 || dbInitConns <= 0
					|| dbMaxConns <= 0 || dbMaxConns < dbInitConns) {
				throw new BExceptions(FwConstants.PCodes.INVALID_VALUE,
						ctx + ": one or more configurations are not valid!");
			}
		}
	}

	private class DBConnectionW {
		Connection conn;
		long lastUsedTS;
	}

	private class DBPool {
		String url;
		DBConnData dbConnData;
		Stack<DBConnectionW> connections;
		DBMutex mutex = new DBMutex();
		boolean bThrowOnNoData;
		DBPoolMonitor poolMonitorTask;
		Timer poolMonitorTimer;
		String poolName;
		int numConns = 0;

		public DBPool(String poolName, DBConnData dbConnData) {
			url = String.format("jdbc:postgresql://%s:%d/%s?ApplicationName=%s&useSSL=%s", dbConnData.dbHost,
					dbConnData.dbPort, dbConnData.dbName, appName, String.valueOf(dbConnData.bEnableSSL));
			this.dbConnData = dbConnData;
			connections = new Stack<DBConnectionW>();
			bThrowOnNoData = dbConnData.bThrowOnNoData;
			this.poolName = poolName;
		}

		public void init(BLogger logger) throws BExceptions {
			try {
				logger.debug("DBPool: creating " + dbConnData.dbInitConns + " connections");
				long ct = System.currentTimeMillis();
				for (int idx = 0; idx < dbConnData.dbInitConns; idx++) {
					Connection conn = createNewConnection(logger);
					DBConnectionW dbcw = new DBConnectionW();
					dbcw.conn = conn;
					dbcw.lastUsedTS = ct;
					connections.push(dbcw);
					numConns++;
				}

				poolMonitorTask = new DBPoolMonitor(this);
				poolMonitorTimer = new Timer(poolName);
				poolMonitorTimer.scheduleAtFixedRate(poolMonitorTask, dbConnData.minEvictableIdleTimeSecs * 1000,
						dbConnData.minEvictableIdleTimeSecs * 1000);
			} catch (SQLException e) {
				new BExceptions(e, FwConstants.PCodes.INVALID_VALUE);
			}
		}

		public Connection getConnectionRaw(BLogger logger) throws SQLException {
			Connection conn = null;
			mutex.lock(logger);
			try {
				int size = connections.size();
				conn = connections.pop().conn;
				logger.debug("DBPool: pool is NOT empty, returning connection " + conn + " available: " + size
						+ " in use: " + (numConns - size));
			} catch (Exception ex) {
				throw ex;
			} finally {
				mutex.unlock();
			}
			
			return conn;
		}

		public Connection createNewConnection(BLogger logger) throws SQLException {
			Connection conn = DriverManager.getConnection(url, dbConnData.dbUser, dbConnData.dbPwd);			
			testConnection(logger, conn);
			return conn;
		}
		
		public Connection getConnection(BLogger logger) throws SQLException {
			Connection conn = null;
			PerfMonData pd = new PerfMonData("", "", MonEvent.DB, "getConnection");
			try {
				conn = getConnectionRaw(logger);
			} catch (EmptyStackException ex) {
				conn = createNewConnection(logger);

				logger.debug("DBPool: created new connection " + conn);
				
				mutex.lock(logger);
				numConns++;
				mutex.unlock();
			} finally {
				pd.conclude();
				pd.log(logger);
			}
			return conn;
		}

		public void releaseConnection(BLogger logger, Connection conn) {
			try {
				mutex.lock(logger);
				DBConnectionW dbcw = new DBConnectionW();
				dbcw.conn = conn;
				dbcw.lastUsedTS = System.currentTimeMillis();

				connections.push(dbcw);
			} finally {
				mutex.unlock();
			}
		}

		private void testConnection(BLogger logger, Connection conn) throws SQLException {
			logger.debug("DBPool: testing connection " + conn);
			PreparedStatement ps = conn.prepareStatement("SELECT 1");
			ps.execute();
			logger.debug("DBPool: connection test successful " + conn);
		}

		private class DBPoolMonitor extends TimerTask {
			DBPool _pool;
			BLogger logger = null;

			DBPoolMonitor(DBPool pool) {
				_pool = pool;
			}

			@Override
			public void run() {
				if(logger == null) {
					logger = BLoggerFactory.create(_pool.poolName, 0, "DBPoolMonitor");
				}
				
				_pool.mutex.lock(logger);
				//long curTS = System.currentTimeMillis();
				logger.debug("DBPoolMonitor: Checking pool " + _pool.connections.size());
				List<DBConnectionW> conns2ReleaseBack = new ArrayList<DBConnectionW>();
				while (!_pool.connections.isEmpty()) {
					DBConnectionW conn = connections.pop();
					try {
						_pool.testConnection(logger, conn.conn);
						conns2ReleaseBack.add(conn);
					} catch (Exception ex) {
						logger.error("DBPoolMonitor: Connection failed, will be removed from pool " + conn.conn);
						logger.error(ex);
						numConns--;
						if(conn.conn != null) {
							try {
								logger.error("DBPoolMonitor: Closing Connection " + conn.conn);
								conn.conn.close();
							} catch (SQLException e) {
								logger.error(ex);
							}
							conn.conn = null;
						}
					}
				}
				for (DBConnectionW conn : conns2ReleaseBack) {
					_pool.connections.push(conn);
				}
				_pool.mutex.unlock();
			}
		}
	}

	class DBMutex {
		ReentrantLock mutex = new ReentrantLock();

		public void lock(BLogger logger) {
			PerfMonData pd = new PerfMonData("", "", MonEvent.DB, "LOCK");
			mutex.lock();
			pd.conclude();
			pd.log(logger);
		}

		public void unlock() {
			mutex.unlock();
		}
	}
}
