package com.bundee.msfw.servicefw.logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;

public class BLoggerFactory {
	private static BLoggerFactory blf = new BLoggerFactory();
	private static BLogger dummyLogger = blf.new DymmyLogger();
	private static String thisHost = "localhost";
	

	private final static String pattern = "[%d{yyyy-MM-dd HH:mm:ss,SSS}] [%-5p] %m%n";

	public static String LOG_MAX_FILE_SIZE_PNAME = "logging.maxFileSize";
	public static String LOG_MAX_ROLLING_IDX_PNAME = "logging.maxRollingIdx";
	public static String LOG_LEVEL_PNAME = "logging.logLevel";
	public static String LOG_FILE_COMPRESSION_STATE = "logging.compressFile";
	public static String LOG_MAX_EX_LINES = "logging.maxExceptionLines";
	public static String LOG_EVENTS_IN_DB = "logging.event.db";

	private final static Level DEFAULT_LL = Level.WARN;
	private final static int DEFAULT_MAX_FILE_SIZE = 1048576; // 1MB
	private final static int DEFAULT_COMPRESSION_STATE = 0; // 0=COMPRESSION DISABLED, 1=COMPRESSION ENABLED
	private final static int DEF_NUM_STACK_ELEMENTS_TO_LOG = 10;

	private static int maxExLines;

	private static DBLogSerializer dbLogs = null;
	private static boolean bNeedDBLogger = false;
	private static boolean bUseDBForEvent = true;
	
	
	public static void setThisHost(String host) {
		thisHost = host;
	}
	public static void setDBLogSerializer(DBLogSerializer dbLogsIn) {
		dbLogs = dbLogsIn;
	}

	public static void needDBLogger(boolean bNeedDBLoggerP) {
		bNeedDBLogger = bNeedDBLoggerP;
	}

	public static void useDBForEvent(boolean bUseDBForEventP) {
		bUseDBForEvent = bUseDBForEventP;
	}
	
	public static boolean needDBLogger() {
		return bNeedDBLogger;
	}
	
	public static boolean useDBForEvent() {
		return bUseDBForEvent;
	}
	
	public static BLogger getDummyLogger() {
		return dummyLogger;
	}

	public static BLogger create(String modid, long reqID, String baName) {
		BLogger logger = dummyLogger;
		if (bNeedDBLogger) {
			if (dbLogs != null) {
				logger = blf.new BDBLogger(modid, baName);
			}
		} else {
			logger = blf.new BFileLogger(modid, reqID, baName);
		}
		
		return logger;
	}

	private static Level getLoglevel(FileCfgHandler fch) {
		int lvl = fch.getCfgParamInt(LOG_LEVEL_PNAME);
		Level ll = DEFAULT_LL;
		switch (lvl) {
		case 0:
			ll = Level.FATAL;
			break;
		case 1:
			ll = Level.WARN;
			break;
		case 2:
			ll = Level.ERROR;
			break;
		case 3:
			ll = Level.INFO;
			break;
		case 4:
			ll = Level.DEBUG;
			break;
		case 5:
			ll = Level.TRACE;
			break;
		default:
			ll = Level.INFO;
			break;
		}
		return ll;
	}

	public static BLogger init(FileCfgHandler fch, String modid) {
		if (bNeedDBLogger) {
			if (dbLogs == null) {
				return dummyLogger;
			} else {
				return blf.new BDBLogger(modid, "run");
			}
		} else {
			String serviceName = fch.getApplication().getName();
			String lfDir = fch.getApplication().getLogsFolder();
			String lfName = serviceName + ".log";
			String mn = "run";
			if (modid.equalsIgnoreCase("startup")) {
				lfName = serviceName + "_startup.log";
				mn = "init";
			}

			initFileLogger(fch, lfDir, lfName);
			return create(modid, 0, mn);
		}
	}

	private static void initFileLogger(FileCfgHandler fch, String lfDir, String lfName) {
		Level ll = getLoglevel(fch);
		String logFP = lfDir + File.separator + lfName;

		int maxFileSize = fch.getCfgParamInt(LOG_MAX_FILE_SIZE_PNAME);
		if (maxFileSize == -1) {
			maxFileSize = DEFAULT_MAX_FILE_SIZE;
		}

		int compressionRequired = fch.getCfgParamInt(LOG_FILE_COMPRESSION_STATE);
		String logFilePattern = logFP + "-%d{MM-dd-yy}.gz";
		if (compressionRequired == -1) {
			compressionRequired = DEFAULT_COMPRESSION_STATE;
			logFilePattern = logFP + "-%d{MM-dd-yy}";
		}

		maxExLines = fch.getCfgParamInt(LOG_MAX_EX_LINES);
		if (maxExLines <= 0) {
			maxExLines = DEF_NUM_STACK_ELEMENTS_TO_LOG;
		}

		if(fch.getAllCfgParams().containsKey(LOG_EVENTS_IN_DB)) {
			bUseDBForEvent = Boolean.parseBoolean(fch.getCfgParamStr(LOG_EVENTS_IN_DB));
		}
		
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setConfigurationName("RollingBuilder");
		builder.setStatusLevel(Level.ERROR);
		LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern", pattern);

		ComponentBuilder<?> triggeringPolicy = builder.newComponent("Policies")
				.addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
				.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", maxFileSize));

		AppenderComponentBuilder appenderBuilder = builder.newAppender("rolling", "RollingFile")
				.addAttribute("fileName", logFP).addAttribute("filePattern", logFilePattern).add(layoutBuilder)
				.addComponent(triggeringPolicy);

		builder.add(appenderBuilder);

		builder.add(builder.newRootLogger(ll).add(builder.newAppenderRef("rolling")));
		builder.add(builder.newLogger(lfName, ll).add(builder.newAppenderRef("rolling")));
		Configuration configuration = builder.build();
		Configurator.initialize(configuration);
		LoggerContext.getContext().reconfigure(configuration);
	}

	class BFileLogger implements BLogger {
		private Logger logger;
		private String modid;
		private String baName;
		private final long reqID;
		private Set<Throwable> alreadyLoggedExSet;

		BFileLogger(String modid, long reqID, String baName) {
			this.modid = modid;
			this.baName = baName;
			this.reqID = reqID;
			this.alreadyLoggedExSet = new HashSet<Throwable>();
			logger = LoggerContext.getContext().getLogger(Long.toString(reqID));
		}

		private String getMsgWithPfx(String msg) {
			String pfx = String.format("[%d][%d][%s][%s] - ", Thread.currentThread().getId(), reqID, modid, baName);
			return pfx + msg;
		}

		public void trace(String msg) {
			logger.trace(getMsgWithPfx(msg));
		}

		public void debug(String msg) {
			logger.debug(getMsgWithPfx(msg));
		}

		public void info(String msg) {
			logger.info(getMsgWithPfx(msg));
		}

		public void error(String msg) {
			logger.error(getMsgWithPfx(msg));
		}

		public void error(Throwable ex) {
			StringBuffer mb = new StringBuffer();
			logError(ex, mb, "");
			logger.error(getMsgWithPfx(mb.toString()));
		}

		public void warn(String msg) {
			logger.warn(getMsgWithPfx(msg));
		}

		public void fatal(String msg) {
			logger.fatal(getMsgWithPfx(msg));
		}

		public void event(String msg) {
			if(dbLogs != null) {
				dbLogs.insertLogNow(thisHost, Thread.currentThread().getId(), "event", modid, baName, msg);
			} else {
				logger.info(getMsgWithPfx(msg));
			}
		}
		
		public PrintStream getPS() {
			LogStream ls = blf.new LogStream(this);
			return new PrintStream(ls);
		}

		@Override
		public String getModID() {
			return modid;
		}

		private void logError(Throwable ex, StringBuffer mb, String pfx) {
			if (ex == null || alreadyLoggedExSet.contains(ex))
				return;
			alreadyLoggedExSet.add(ex);
			StackTraceElement[] ses = ex.getStackTrace();
			mb.append(pfx).append(ex.getMessage());
			int numLines2Log = (ses.length < maxExLines ? ses.length : maxExLines);
			for (int idx = 0; idx < numLines2Log; idx++) {
				mb.append("\n").append("\t").append(ses[idx].toString());
			}
			logError(ex.getCause(), mb, "\nCaused by: ");
		}
	}

	class LogStream extends FilterOutputStream {
		private BLogger logger = null;
		private ByteArrayOutputStream bos = new ByteArrayOutputStream();

		public LogStream(BLogger logger) {
			super(null);
			super.out = bos;
			this.logger = logger;
		}

		@Override
		public void flush() throws IOException {
			// this was never called in my test
			bos.flush();
			if (bos.size() > 0) {
				logger.info(bos.toString());
			}
			bos.reset();
		}

		@Override
		public void write(byte[] b) throws IOException {
			logger.info(new String(b));
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			logger.info(new String(b, off, len));
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b });
		}
	}

	class BDBLogger implements BLogger {

		private String modid;
		private String baName;
		private Set<Throwable> alreadyLoggedExSet;

		BDBLogger(String modid, String baName) {
			this.modid = modid;
			this.baName = baName;
			this.alreadyLoggedExSet = new HashSet<Throwable>();
		}

		public void trace(String msg) {
			dbLogs.insertLog(thisHost, Thread.currentThread().getId(), "trace", modid, baName, msg);
		}

		public void debug(String msg) {
			dbLogs.insertLog(thisHost, Thread.currentThread().getId(), "debug", modid, baName, msg);
		}

		public void info(String msg) {
			dbLogs.insertLog(thisHost, Thread.currentThread().getId(), "info", modid, baName, msg);
		}

		public void error(String msg) {
			dbLogs.insertLog(thisHost, Thread.currentThread().getId(), "error", modid, baName, msg);
		}

		public void error(Throwable ex) {
			StringBuffer mb = new StringBuffer();
			logError(ex, mb, "");
			error(mb.toString());
		}

		public void warn(String msg) {
			dbLogs.insertLog(thisHost, Thread.currentThread().getId(), "warn", modid, baName, msg);
		}

		public void fatal(String msg) {
			dbLogs.insertLog(thisHost, Thread.currentThread().getId(), "fatal", modid, baName, msg);
		}

		public void event(String msg) {
			dbLogs.insertLogNow(thisHost, Thread.currentThread().getId(), "event", modid, baName, msg);
		}
		
		public PrintStream getPS() {
			LogStream ls = blf.new LogStream(this);
			return new PrintStream(ls);
		}

		@Override
		public String getModID() {
			return modid;
		}

		private void logError(Throwable ex, StringBuffer mb, String pfx) {
			if (ex == null || alreadyLoggedExSet.contains(ex))
				return;
			alreadyLoggedExSet.add(ex);
			StackTraceElement[] ses = ex.getStackTrace();
			mb.append(pfx).append(ex.getMessage());
			int numLines2Log = (ses.length < maxExLines ? ses.length : maxExLines);
			for (int idx = 0; idx < numLines2Log; idx++) {
				mb.append("\n").append("\t").append(ses[idx].toString());
			}
			logError(ex.getCause(), mb, "\nCaused by: ");
		}
	}

	class DymmyLogger implements BLogger {

		@Override
		public void trace(String msg) {
		}

		@Override
		public void debug(String msg) {
		}

		@Override
		public void info(String msg) {
		}

		@Override
		public void error(String msg) {
		}

		@Override
		public void warn(String msg) {
		}

		@Override
		public void fatal(String msg) {
		}

		@Override
		public void event(String msg) {
		}
		
		@Override
		public void error(Throwable ex) {
		}

		@Override
		public PrintStream getPS() {
			return null;
		}

		@Override
		public String getModID() {
			return null;
		}
	}
}
