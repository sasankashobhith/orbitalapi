package com.bundee.msfw.servicefw.fw;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.fcfgi.Application;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.msfw.servicefw.authz.*;
import com.bundee.msfw.servicefw.blmods.*;
import com.bundee.msfw.servicefw.dbm.*;
import com.bundee.msfw.servicefw.fw.URLPathProcessor.*;
import com.bundee.msfw.servicefw.jobs.*;
import com.bundee.msfw.servicefw.logger.*;
import com.bundee.msfw.servicefw.srvutils.cache.*;
import com.bundee.msfw.servicefw.srvutils.config.*;
import com.bundee.msfw.servicefw.srvutils.job.*;
import com.bundee.msfw.servicefw.srvutils.monitor.*;
import com.bundee.msfw.servicefw.srvutils.utils.*;
import org.apache.cxf.jaxrs.ext.multipart.*;
import org.springframework.core.io.*;
import org.springframework.core.io.support.*;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

public class ServiceFramework {
    private static final String ROOT_PROG_ARG = "-root";
    private static ServiceFramework gSvcFramework = new ServiceFramework();
    private static GlobalServiceCapsule globalServiceCapsule = GlobalServiceCapsule.getInstance();
    private FileCfgHandlerImpl fileCfgHandler = new FileCfgHandlerImpl();
    private DBManagerFactory dbmf = new DBManagerFactory();
    private BLAuthzModule authzModule = null;
    private List<String> blClassFQDNs = new ArrayList<String>();
    private BLogger gLogger;
    private boolean bServiceNotReady = true;
    private URLPathProcessor urlPathProcessor = new URLPathProcessor();
    private BScheduler scheduler = null;
    private JobDistributor jobDistributor = null;
    private AtomicLong reqID = new AtomicLong(0);

    private ServiceFramework() {
        ServiceInitializer.init();
    }

    public static ServiceFramework getInstance() {
        return gSvcFramework;
    }

    private static void logExceptionList(BLogger logger, BExceptions exceptions) {
        exceptions.printStackTrace(logger);
    }

    public static UTF8String generateStdResponse(BLogger logger, BJson jsonProc, BExceptions exceptions) {
        BResponseCodes respCodes = new BResponseCodes();
        try {
            if (!exceptions.hasExceptions()) {
                respCodes.add(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY);
            } else {
                respCodes.add(exceptions);
            }
        } catch (Exception e) {
            logger.error(e);
            respCodes.add(e, FwConstants.PCodes.INTERNAL_ERROR);
        }
        BaseResponse respObj = new BaseResponse();
        respObj.setCodes(respCodes.getCodes());
        return jsonProc.toJson(respObj);
    }

    private static void readCookiesFromHeader(BLogger logger, Map<String, List<String>> reqHeaders,
                                              RequestContextImpl reqCtx) {
        List<String> cookieStrList = reqHeaders.get("Cookie");
        if (cookieStrList != null && !cookieStrList.isEmpty()) {
            for (String cookieStr : cookieStrList) {
                if (cookieStr == null || cookieStr.isBlank())
                    continue;
                String[] cookieStrs = cookieStr.split(";");
                for (String oneCookie : cookieStrs) {
                    List<HttpCookie> cookies = HttpCookie.parse(oneCookie);
                    if (cookies != null && !cookies.isEmpty()) {
                        for (HttpCookie hcookie : cookies) {
                            reqCtx.setInCookie(hcookie.getName(), hcookie);
                        }
                    }
                }
            }
        }
    }

    private static void writeCookiesToHeaders(BLogger logger, RequestContextImpl reqCtx,
                                              Map<String, List<String>> respHeaders) {
        Map<String, HttpCookie> outCookies = reqCtx.getAllOutCookies();
        List<String> cookieStringList = new ArrayList<String>();
        if (outCookies != null && !outCookies.isEmpty()) {
            outCookies.forEach((name, cookie) -> {
                StringBuffer sb = new StringBuffer();
                sb.append(cookie.getName()).append("=").append(cookie.getValue());
                if (cookie.getPath() != null) {
                    sb.append("; path=").append(cookie.getPath());
                }
                if (cookie.getMaxAge() >= 0) {
                    sb.append("; Max-Age=").append(cookie.getMaxAge());
                }
                if (cookie.isHttpOnly()) {
                    sb.append("; HttpOnly");
                }
                if (cookie.getSecure()) {
                    sb.append("; Secure");
                }
                String cs = sb.toString();
                cookieStringList.add(cs);
            });
        }
        if (!cookieStringList.isEmpty()) {
            respHeaders.put("Set-Cookie", cookieStringList);
        }
    }

    public Application getApplication() {
        return fileCfgHandler.getApplication();
    }

    public String getValueFromVault(String key) throws BExceptions {
        return globalServiceCapsule.getVaultService().getValue(gLogger, key);
    }

    // * Following is the structure assumed here. This is done deliberately to
    // enforce a structured deployment.
    // * <ROOT>/bin - application binaries like the main executable jar, start/stop
    // scripts...
    // * <ROOT>/conf - configuration folder where all .properties files will reside.
    // * <ROOT>/certs - certificate configuration folder where all PKCS12/JKS files
    // reside.
    // * <ROOT>/logs - folder where all log files would reside.
    private List<String> preInit(String[] args, BExceptions exceptions) {
        String rootPath = Utils.getValueFromArgs(args, ROOT_PROG_ARG);
        globalServiceCapsule.fch = fileCfgHandler;
        BLoggerFactory.setThisHost(fileCfgHandler.getApplication().getThisHost());
        List<String> loadedProsFiles = new ArrayList<String>();
        ApplicationDetails app = (ApplicationDetails) fileCfgHandler.getApplication();
        if (rootPath != null && !rootPath.isBlank()) {
            File rootFolder = new File(rootPath);
            if (rootFolder == null || !rootFolder.exists() || !rootFolder.isDirectory()) {
                exceptions.add(
                        new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Root path " + rootPath + " is invalid!"));
            }
            // No point in continuing if root itself is not correct!
            if (exceptions.hasExceptions())
                return loadedProsFiles;
            rootPath = Utilities.endWithSlash(rootPath);
            String confPath = rootPath + "conf" + File.separator;
            String certPath = rootPath + "certs" + File.separator;
            String logPath = rootPath + "logs" + File.separator;
            fileCfgHandler.addCfgParam("rootPath", rootPath);
            File confFolder = new File(confPath);
            File certFolder = new File(certPath);
            File logFolder = new File(logPath);
            if (!certFolder.exists() || !certFolder.isDirectory() || !certFolder.canRead()) {
                System.out.println("Certificates path is not correct or not readable - " + certPath);
            }
            if (!logFolder.exists() || !logFolder.isDirectory()) {
                exceptions.add(
                        new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Logs path is not correct - " + logPath));
            }
            if (!confFolder.exists() || !confFolder.isDirectory()) {
                exceptions.add(new BExceptions(FwConstants.PCodes.INVALID_VALUE,
                        "Configuration path is not correct - " + confPath));
            } else {
                for (String cfgFile : confFolder.list()) {
                    if (cfgFile.toLowerCase().endsWith(".properties")) {
                        String fpath = confPath + cfgFile;
                        fileCfgHandler.loadPropertiesFile(fpath);
                        loadedProsFiles.add(fpath);
                    }
                }
            }
            app.setRootFolder(rootPath);
            app.setConfFolder(confPath);
            app.setCertsFolder(certPath);
            app.setLogsFolder(logPath);
            ApplicationDetails.setUsingResource(false);
        } else {
            ApplicationDetails.setUsingResource(true);
            loadPropertiesFromResources(loadedProsFiles);
            BLoggerFactory.needDBLogger(true);
        }
        String serviceName = fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.SRV_NAME.getValue());
        if (serviceName == null || serviceName.isBlank()) {
            serviceName = "undefined-service";
        }
        Integer port = fileCfgHandler.getCfgParamInt(CommonFileCfgDefs.GEN_CFG_PARAMS.SRV_PORT.getValue());
        app.setPort(port);
        fileCfgHandler.setAppName(serviceName);
        readBLModuleFQDNs();
        return loadedProsFiles;

    }

    private void loadPropertiesFromResources(List<String> loadedProsFiles) {
        try {
            ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourceResolver.getResources("classpath*:/*.properties");
            for (int idx = 0; idx < resources.length; idx++) {
                try {
                    Resource r = resources[idx];
                    if (!r.getFilename().equalsIgnoreCase("pom.properties")) {
                        File f = r.getFile();
                        loadedProsFiles.add(f.getAbsolutePath());
                        fileCfgHandler.loadPropertiesFile(new FileInputStream(f));
                    }
                } catch (FileNotFoundException fex) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readBLModuleFQDNs() {
        Map<String, Object> allParams = fileCfgHandler.getAllCfgParams();
        boolean bFoundBLMods = false;
        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith(CommonFileCfgDefs.CLS_LOADER_PARAMS.BL_MODULES_PFX_NEW)) {
                blClassFQDNs.add((String) entry.getValue());
                bFoundBLMods = true;
            }
        }
        if (!bFoundBLMods) {
            String blModuleCLS = fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.CLS_LOADER_PARAMS.BL_MODULES_OLD);
            String[] blModsArray = blModuleCLS == null || blModuleCLS.isBlank() ? null : blModuleCLS.split(",");
            if (blModsArray != null) {
                blClassFQDNs.addAll(Arrays.asList(blModsArray));
            }
        }
    }

    private Map<String, Object> setSpringBootConfigs() throws BExceptions {
        Map<String, Object> sbParams = new HashMap<String, Object>();
        Integer port = fileCfgHandler.getCfgParamInt(CommonFileCfgDefs.GEN_CFG_PARAMS.SRV_PORT.getValue());
        sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.SRV_PORT.getValue(), port);
        sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.APP_NAME.getValue(), fileCfgHandler.getApplication().getName());
        String srvCfgPfx = fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.SRV_CFG_PFX.getValue());
        String srvSSLParam = CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.SSL_ENABLED.getValue();
        if (srvCfgPfx != null && !srvCfgPfx.isBlank()) {
            srvSSLParam = srvCfgPfx + "." + srvSSLParam;
        }
        boolean bSSLEnabled = Boolean.parseBoolean(fileCfgHandler.getCfgParamStr(srvSSLParam));
        if (bSSLEnabled) {
            if (globalServiceCapsule.vaultService == null) {
                throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
                        "Vault service is mandatory for extracting SSL password");
            }
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.SSL_ENABLED.getValue(), true);
            String keyStorePath = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                    CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.KEYSTORE_PATH.getValue(), fileCfgHandler);
            keyStorePath = fileCfgHandler.getApplication().makeAbsolutePathFromCert(keyStorePath);
            String keyStoreType = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                    CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.KEYSTORE_TYPE.getValue(), fileCfgHandler);
            String keyStorepwdKey = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                    CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.KEYSTORE_PWD_KEY.getValue(), fileCfgHandler);
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.KS_TYPE.getValue(), keyStoreType);
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.KS_PATH.getValue(), keyStorePath);
            char[] kspwd = globalServiceCapsule.vaultService.getValue(gLogger, keyStorepwdKey).toCharArray();
            String keyStorepwd = new String(kspwd);
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.KS_PWD.getValue(), keyStorepwd);
            boolean bClientAuth = true;
            try {
                String clientAuthStr = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                        CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.SSL_ENABLE_CLIENT_AUTH.getValue(), fileCfgHandler);
                if (clientAuthStr != null && !clientAuthStr.isBlank()) {
                    bClientAuth = Boolean.parseBoolean(clientAuthStr);
                }
            } catch (Exception ex) {
            }
            if (bClientAuth) {
                sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.CLIENT_AUTH.getValue(),
                        CommonFileCfgDefs.SSL_CLIENT_AUTH_NEED);
                String trustStorePath = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                        CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TRUSTSTORE_PATH.getValue(), fileCfgHandler);
                trustStorePath = fileCfgHandler.getApplication().makeAbsolutePathFromCert(trustStorePath);
                String trustStoreType = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                        CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TRUSTSTORE_TYPE.getValue(), fileCfgHandler);
                String trustStorepwdKey = CommonFileCfgDefs.checkAndGetParam(srvCfgPfx,
                        CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TRUSTSTORE_PWD_KEY.getValue(), fileCfgHandler);
                char[] tspwd = globalServiceCapsule.vaultService.getValue(gLogger, trustStorepwdKey).toCharArray();
                String trsutStorepwd = new String(tspwd);
                sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.SSL_TRUST_STORE.getValue(), trustStorePath);
                sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.TS_TYPE.getValue(), trustStoreType);
                sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.SSL_TRUST_STORE_PWD.getValue(), trsutStorepwd);
            } else {
                sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.CLIENT_AUTH.getValue(),
                        CommonFileCfgDefs.SSL_CLIENT_AUTH_WANT);
            }
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.SSL_PROTOCOL.getValue(), CommonFileCfgDefs.DEF_SSL_PROTOCOL);
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.TLS_PROTOCOLS.getValue(), CommonFileCfgDefs.DEF_PROTOCOLS);
            sbParams.put(CommonFileCfgDefs.SB_CFG_PARAMS.TLS_CIPHERS.getValue(), CommonFileCfgDefs.DEF_TLS_CIPHERS);
        }
        return sbParams;
    }

    public void checkAndThrow(BLogger logger, BExceptions exceptions) throws BExceptions {
        if (exceptions.hasExceptions()) {
            if (logger != null) {
                logExceptionList(logger, exceptions);
                logger.fatal(fileCfgHandler.getApplication().getName() + " failed due to previous errors!");
            }
            throw exceptions;
        }
    }

    public Map<String, Object> init(String[] args) {
        Map<String, Object> sbParams = null;
        try {
            TLSDataCapsule.makeNewThreadLocalData();
            initImpl(args);
            Map<String, Object> sbParamsLocal = setSpringBootConfigs();
            if (sbParamsLocal.isEmpty()) {
                sbParams = fileCfgHandler.getAllCfgParams();
            } else {
                sbParams = new HashMap<String, Object>();
                sbParams.putAll(fileCfgHandler.getAllCfgParams());
                sbParams.putAll(sbParamsLocal);
            }
        } catch (BExceptions e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return sbParams;
    }

    public void initImpl(String[] args) throws BExceptions {
        BExceptions exceptions = new BExceptions();
        List<String> loadedProsFiles = preInit(args, exceptions);
        checkAndThrow(gLogger, exceptions);
        gLogger = BLoggerFactory.init(fileCfgHandler, "startup");
        DCacheMgrFactory dCacheMgrFactory = new DCacheMgrFactory(DCacheMgrFactory.DC_SVC_TYPE_APIS, fileCfgHandler);
        boolean bEnableSSLTrace = Boolean.parseBoolean(
                fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.SRV_CFG_ENABLE_SSL_TRACE.getValue()));
        if (bEnableSSLTrace) {
            System.setProperty("javax.net.debug", "ssl:handshake");
            // Ref:
            // https://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#Debug
        }
        globalServiceCapsule.setCacheManager(dCacheMgrFactory.getCacheManagerImpl());
        globalServiceCapsule.bPerfMonEnabled = true;
        if (fileCfgHandler.getAllCfgParams()
                .containsKey(CommonFileCfgDefs.GEN_CFG_PARAMS.MON_ENABLE_PERF_MON.getValue())) {
            globalServiceCapsule.bPerfMonEnabled = Boolean.parseBoolean(
                    fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.MON_ENABLE_PERF_MON.getValue()));
        }
        gLogger.info("Loaded properties from following files - ");
        loadedProsFiles.forEach(cfgp -> gLogger.info(cfgp));
        urlPathProcessor.init(gLogger);
        initServices(exceptions);
        checkAndThrow(gLogger, exceptions);
        BLModServicesImpl modServices = new BLModServicesImpl(globalServiceCapsule);
        Set<HealthCheck> hcBLModules = new HashSet<HealthCheck>();
        loadAndInitBLMods(modServices, hcBLModules, exceptions);
        checkAndThrow(gLogger, exceptions);
        loadAuthzClient(modServices, exceptions);
        Map<String, JobReccurence> jobRecurrence = new HashMap<String, JobReccurence>();
        Map<String, RecurringJob> jobs = new HashMap<String, RecurringJob>();
        loadJobs(jobs, jobRecurrence, hcBLModules, exceptions);
        checkAndThrow(gLogger, exceptions);
        int initialDelaySecs = fileCfgHandler
                .getCfgParamInt(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_INITIAL_DELAY_SECS.getValue());
        scheduler = new BScheduler();
        scheduler.init(gLogger, jobDistributor, initialDelaySecs, globalServiceCapsule, jobRecurrence, jobs,
                exceptions);
        checkAndThrow(gLogger, exceptions);
        gLogger.info("Scheduled [" + jobs.size() + "] Jobs: ");
        for (Map.Entry<String, RecurringJob> jpair : jobs.entrySet()) {
            gLogger.info(jpair.getValue().getClass().getSimpleName() + " : " + jobRecurrence.get(jpair.getKey())
                    + " seconds");
        }
        bServiceNotReady = false;
        globalServiceCapsule.resetRESTClientIniterFactoryImpl();
        if (globalServiceCapsule.bPerfMonEnabled) {
            gLogger.info("Performance Monitoring is Enabled!");
        } else {
            gLogger.info("Performance Monitoring is NOT Enabled!");
        }
        if (globalServiceCapsule.bSvcMonEnabled) {
            gLogger.info("Service Monitoring is Enabled!");
        } else {
            gLogger.info("Service Monitoring is NOT Enabled!");
        }
        if (globalServiceCapsule.bCertMonEnabled) {
            gLogger.info("Certificate Monitoring is Enabled!");
        } else {
            gLogger.info("Certificate Monitoring is NOT Enabled!");
        }
        gLogger.info(fileCfgHandler.getApplication().getName() + " initialization complete");
        scheduler.start(gLogger);
        gLogger = BLoggerFactory.init(fileCfgHandler, "runtime");
    }

    public void stop() {
        int shutDownDelaySecs = fileCfgHandler
                .getCfgParamInt(CommonFileCfgDefs.GEN_CFG_PARAMS.SHUTDOWN_INITIAL_DELAY_SECS.getValue());
        gLogger.info(fileCfgHandler.getApplication().getName() + " stopping...");
        bServiceNotReady = true;
        scheduler.stop();
        if (shutDownDelaySecs <= 0)
            shutDownDelaySecs = 10;
        try {
            Thread.sleep(shutDownDelaySecs * 1000);
        } catch (InterruptedException e) {
            gLogger.error(e);
        }
        gLogger.info(fileCfgHandler.getApplication().getName() + " stopped");
    }

    private void initServices(BExceptions exceptions) {
        try {
            gLogger.info("Initializing Primary services!");
            ServiceInitializer.initializeServices(gLogger, fileCfgHandler, globalServiceCapsule, exceptions);
            gLogger.info("Initializing DB service!");
            dbmf.initializeDB(gLogger, fileCfgHandler, globalServiceCapsule.getVaultService());
            if (BLoggerFactory.needDBLogger() || BLoggerFactory.useDBForEvent()) {
                int periodSecs = fileCfgHandler.getCfgParamInt("db.log.periodSecs");
                periodSecs = (periodSecs < 5 ? 5 : periodSecs);
                DBLogSerializer dblogs = new DBLogSerializer(dbmf, periodSecs, !BLoggerFactory.needDBLogger());
                BLoggerFactory.setDBLogSerializer(dblogs);
            }
            globalServiceCapsule.dbmf = dbmf;
            gLogger.info("Initializing Custom services!");
            ServiceInitializer.initializeCustomServices(gLogger, fileCfgHandler, globalServiceCapsule, exceptions);
            gLogger = BLoggerFactory.create("startup", 0, "init");
        } catch (BExceptions eList) {
            exceptions.add(eList);
        }
    }

    private void loadJobs(Map<String, RecurringJob> jobs, Map<String, JobReccurence> jobRecurrence,
                          Set<HealthCheck> hcBLModules, BExceptions exceptions) {
        for (Map.Entry<String, Object> pair : fileCfgHandler.getAllCfgParams().entrySet()) {
            if (pair.getKey().startsWith("job.")) {
                String jClassName = pair.getKey().substring(pair.getKey().indexOf("job.") + 4);
                String value = (String) pair.getValue();
                JobReccurence jr;
                try {
                    jr = BScheduler.getJobRecInstance(value);
                    RecurringJob jobObj = (RecurringJob) ServiceInitializer.loadClass(jClassName,
                            RecurringJob.class.getName(), null, exceptions);
                    if (jobObj != null) {
                        jobs.put(jClassName, jobObj);
                        jobRecurrence.put(jClassName, jr);
                    }
                } catch (BExceptions e) {
                    exceptions.add(e);
                }
            }
        }
        loadServiceMonitoringJobs(jobs, jobRecurrence, hcBLModules, exceptions);
        loadCertMonitoringJob(jobs, jobRecurrence, hcBLModules, exceptions);
        String dc = fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_DISTRIBUTOR_CLS.getValue());
        if (dc != null && !dc.isBlank()) {
            jobDistributor = (JobDistributor) ServiceInitializer.loadClass(dc, JobDistributor.class.getName(), null,
                    exceptions);
        }
    }

    private void loadServiceMonitoringJobs(Map<String, RecurringJob> jobs, Map<String, JobReccurence> jobRecurrence,
                                           Set<HealthCheck> hcBLModules, BExceptions exceptions) {
        try {
            globalServiceCapsule.bSvcMonEnabled = false;
            boolean bEnableSvcMon = Boolean.parseBoolean(
                    fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.MON_ENABLE_SVC_MON.getValue()));
            if (bEnableSvcMon) {
                globalServiceCapsule.bSvcMonEnabled = true;
                String svcmPeriodStr = fileCfgHandler
                        .getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_JOB_FREQ.getValue());
                JobReccurence jrSVCM = BScheduler.getJobRecInstance(svcmPeriodStr);
                jobs.put(HealthCheckJob.class.getName(), new HealthCheckJob(globalServiceCapsule, hcBLModules));
                jobRecurrence.put(HealthCheckJob.class.getName(), jrSVCM);
                // Notification job needs email service
                if (globalServiceCapsule.getEmailerService() == null) {
                    exceptions.add(new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
                            "Email service configuration is needed for Service Monitor Notifications!"));
                } else {
                    String svcmNotificationPeriodStr = fileCfgHandler
                            .getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_SVC_MON_NOTIFICATION_FREQ.getValue());
                    JobReccurence jrSVCMNotification = BScheduler.getJobRecInstance(svcmNotificationPeriodStr);
                    if (jrSVCMNotification.getRecTS() < jrSVCM.getRecTS()) {
                        jrSVCM.setRecTS(jrSVCMNotification.getRecTS());
                    }
                    jobs.put(SvcMonNotificationJob.class.getName(), new SvcMonNotificationJob());
                    jobRecurrence.put(SvcMonNotificationJob.class.getName(), jrSVCMNotification);
                }
            }
        } catch (BExceptions e) {
            exceptions.add(e);
        }
    }

    private void loadCertMonitoringJob(Map<String, RecurringJob> jobs, Map<String, JobReccurence> jobRecurrence,
                                       Set<HealthCheck> hcBLModules, BExceptions exceptions) {
        try {
            globalServiceCapsule.bCertMonEnabled = false;
            boolean bCerMonEnabled = Boolean.parseBoolean(
                    fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.MON_ENABLE_CERT_MON.getValue()));
            if (bCerMonEnabled) {
                // Notification job needs email service
                if (globalServiceCapsule.getEmailerService() == null) {
                    exceptions.add(new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
                            "Email service configuration is needed for Certificate Monitor Notifications!"));
                } else {
                    globalServiceCapsule.bCertMonEnabled = true;
                    String cmnPeriodStr = fileCfgHandler
                            .getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_FREQ.getValue());
                    JobReccurence jrCertMon = BScheduler.getJobRecInstance(cmnPeriodStr);
                    jobRecurrence.put(CertMonitoringJob.class.getName(), jrCertMon);
                    CertMonitoringJob cmJob = new CertMonitoringJob(fileCfgHandler.getApplication().getCertsFolder());
                    jobs.put(CertMonitoringJob.class.getName(), cmJob);
                    String cmnExpiryNoticePeriodStr = fileCfgHandler.getCfgParamStr(
                            CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_EXPIRY_NOTICE_PERIOD.getValue());
                    JobReccurence jrCertMonNoticePeriod = BScheduler.getJobRecInstance(cmnExpiryNoticePeriodStr);
                    String cmnReminderFreqStr = fileCfgHandler
                            .getCfgParamStr(CommonFileCfgDefs.GEN_CFG_PARAMS.JOB_CERT_MON_REMINDER_FREQ.getValue());
                    JobReccurence jrCertMonReminderFreq = BScheduler.getJobRecInstance(cmnReminderFreqStr);
                    cmJob.setExpiryNoticePeriod(jrCertMonNoticePeriod.getRecTS());
                    cmJob.setReminderFreq(jrCertMonReminderFreq.getRecTS());
                }
            }
        } catch (BExceptions e) {
            exceptions.add(e);
        }
    }

    private void loadAndInitBLMods(BLModServicesImpl modServices, Set<HealthCheck> hcBLModules, BExceptions exceptions)
            throws BExceptions {
        try {
            Set<Object> blObjModules = new HashSet<Object>();
            Set<BLModule> blModules = new HashSet<BLModule>();
            ServiceInitializer.loadClasses(blClassFQDNs, BLModule.class.getName(), null, blObjModules, exceptions);
            checkAndThrow(gLogger, exceptions);
            HealthCheckBLModule hcblmod = new HealthCheckBLModule();
            blObjModules.add(hcblmod);
            initializeBLModules(modServices, gLogger, blObjModules, blModules, exceptions);
            checkAndThrow(gLogger, exceptions);
            Map<String, Set<EndpointData>> blMod2EndpointDetails = urlPathProcessor.registerEndpoints(gLogger,
                    blModules, exceptions);
            checkAndThrow(gLogger, exceptions);
            if (hcBLModules != null) {
                blModules.forEach(blmod -> {
                    if (containsInterface(blmod, HealthCheck.class)) {
                        hcBLModules.add((HealthCheck) blmod);
                    }
                    if (containsInterface(blmod, BLAuthzModule.class)) {
                        authzModule = (BLAuthzModule) blmod;
                    }
                });
            }
            gLogger.info("Loaded BL Modules: " + blMod2EndpointDetails.keySet().toString());
            for (Map.Entry<String, Set<EndpointData>> mpair : blMod2EndpointDetails.entrySet()) {
                String modID = mpair.getKey();
                Set<EndpointData> endpointDetails = mpair.getValue();
                for (EndpointData epd : endpointDetails) {
                    gLogger.info(modID + ":" + epd);
                }
            }
        } catch (ClassCastException e) {
            exceptions.add(new BExceptions(e, FwConstants.PCodes.INTERNAL_ERROR));
        } catch (Exception e) {
            exceptions.add(new BExceptions(e, FwConstants.PCodes.INVALID_VALUE));
        }
    }

    private void loadAuthzClient(BLModServicesImpl modServices, BExceptions exceptions) {
        if (authzModule == null) {
            AuthzClient ac = new AuthzClient();
            BExceptions le = new BExceptions();
            boolean bEnabled = ac.init(gLogger, modServices, le);
            if (bEnabled) {
                exceptions.add(le);
                authzModule = ac;
            }
        }
    }

    private void initializeBLModules(BLModServicesImpl modServices, BLogger logger, Set<Object> blObjModules,
                                     Set<BLModule> blModules, BExceptions exceptions) {
        Set<String> initedMods = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (Object obj : blObjModules) {
            BLModule blMod = (BLModule) obj;
            String blModID = blMod.getClass().getSimpleName();
            if (initedMods.contains(blModID)) {
                exceptions.add(new BExceptions(FwConstants.PCodes.INTERNAL_ERROR,
                        "More than 1 modules have same ModID: " + blModID));
            } else {
                try {
                    blMod.init(logger, modServices);
                    blModules.add(blMod);
                } catch (BExceptions e) {
                    exceptions.add(e);
                }
                initedMods.add(blModID);
            }
        }
    }

    public Response process(String method, String host, String uri, MultivaluedMap<String, String> parameters,
                            MultivaluedMap<String, String> headers, MultipartBody bodyHeaders) {
        BExceptions exceptions = new BExceptions();
        if (bodyHeaders == null || bodyHeaders.getAllAttachments().isEmpty()) {
            exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING,
                    "No Form Data OR Some Form Data Provided is missing");
        }
        MultivaluedMap<String, byte[]> formData = new MultivaluedHashMap<>();
        for (Attachment attachment : bodyHeaders.getAllAttachments()) {
            try {
                formData.add(getFormParamName().apply(attachment.getContentDisposition()),
                        attachment.getDataHandler().getDataSource().getInputStream().readAllBytes());
            } catch (IOException e) {
                exceptions.add(FwConstants.PCodes.INVALID_VALUE, "Form Data provided could not be processed!!! ");
            }
        }
        if (exceptions.hasExceptions()) {
            ResponseBuilder builder = Response.status(exceptions.getHTTPStatus());
            BJson jsonProc = new BJson();
            UTF8String outputJSON = generateStdResponse(gLogger, jsonProc, exceptions);
            builder = builder.entity(outputJSON.getUTF8String());
            return builder.build();
        }
        Map<String, String> formHeaders = bodyHeaders.getAllAttachments().stream()
                .map(Attachment::getContentDisposition)
                .collect(Collectors.toMap(getFormParamName(), getFormParamValue()));
        ReqRespObjCapsule reqRespCapsule = new ReqRespObjCapsule();
        reqRespCapsule.respHeaders = new MultivaluedHashMap<String, String>();
        reqRespCapsule.formData = formData;
        reqRespCapsule.formHeaders = formHeaders;
        reqRespCapsule.setQParameters(parameters);
        reqRespCapsule.setReqHeaders(headers);
        processImpl(method, host, uri, reqRespCapsule);
        ResponseBuilder builder = Response.status(reqRespCapsule.status);
        if (reqRespCapsule.outputJSON != null || reqRespCapsule.outputJSON.getUTF8String() != null) {
            builder = builder.entity(reqRespCapsule.outputJSON.getUTF8String());
        }
        for (Map.Entry<String, List<String>> pair : reqRespCapsule.respHeaders.entrySet()) {
            String name = pair.getKey();
            List<String> vals = pair.getValue();
            for (String val : vals) {
                builder.header(name, val);
            }
        }
        return builder.build();
    }

    public Response process(String method, String host, String uri, MultivaluedMap<String, String> parameters,
                            MultivaluedMap<String, String> headers, String payLoad) {
        ReqRespObjCapsule reqRespCapsule = new ReqRespObjCapsule();
        reqRespCapsule.respHeaders = new MultivaluedHashMap<String, String>();
        reqRespCapsule.payLoad = payLoad;
        reqRespCapsule.setQParameters(parameters);
        reqRespCapsule.setReqHeaders(headers);
        processImpl(method, host, uri, reqRespCapsule);
        ResponseBuilder builder = Response.status(reqRespCapsule.status);
        if (reqRespCapsule.outputJSON != null && reqRespCapsule.outputJSON.getUTF8String() != null) {
            builder = builder.entity(reqRespCapsule.outputJSON.getUTF8String());
        }
        for (Map.Entry<String, List<String>> pair : reqRespCapsule.respHeaders.entrySet()) {
            String name = pair.getKey();
            List<String> vals = pair.getValue();
            for (String val : vals) {
                builder.header(name, val);
            }
        }
        return builder.build();
    }

    public void processImpl(String method, String host, String uri, ReqRespObjCapsule reqRespCapsule) {
        long procTime = System.currentTimeMillis();
        BExceptions erList = new BExceptions();
        BJson jsonProc = new BJson();
        reqRespCapsule.reqLogger = gLogger;
        boolean bIgnoreFurtherPorc = false;
        try {
            List<String> vals = new ArrayList<String>();
            vals.add(host);
            reqRespCapsule.reqHeaders.put(UniversalConstants.HOST_HEADER, vals);
            processRequest(method, jsonProc, uri, reqRespCapsule);
        } catch (BExceptions exceptions) {
            reqRespCapsule.status = exceptions.getHTTPStatus();
            logExceptionList(reqRespCapsule.reqLogger, exceptions);
            reqRespCapsule.outputJSON = generateStdResponse(gLogger, jsonProc, exceptions);
        } catch (Exception e) {
            reqRespCapsule.reqLogger.error(e);
            BExceptions ex = new BExceptions(e, FwConstants.PCodes.INTERNAL_ERROR);
            erList.add(ex);
            reqRespCapsule.outputJSON = generateStdResponse(gLogger, jsonProc, erList);
        } catch (Throwable e) {
            BExceptions ex2 = new BExceptions(e, FwConstants.PCodes.INTERNAL_ERROR);
            reqRespCapsule.reqLogger.error(ex2);
            erList.add(ex2);
            reqRespCapsule.outputJSON = generateStdResponse(gLogger, jsonProc, erList);
        }
        procTime = System.currentTimeMillis() - procTime;
        reqRespCapsule.reqLogger.info("Request END -- [" + procTime + "]");
        if (!bIgnoreFurtherPorc) {
            if (reqRespCapsule.perfMonData != null && reqRespCapsule.outputJSON != null
                    && reqRespCapsule.outputJSON.getUTF8String() != null) {
                reqRespCapsule.perfMonData.setRespSize(reqRespCapsule.outputJSON.getUTF8String().length());
            }
            if (reqRespCapsule.perfTracker != null) {
                reqRespCapsule.perfTracker.endTracking(reqRespCapsule.perfMonData);
                reqRespCapsule.perfTracker.finalizePM(reqRespCapsule.reqLogger,
                        fileCfgHandler.getApplication().getThisHost(), reqRespCapsule.dbm);
            }
        }
    }

    public void processRequest(String method, BJson jsonProc, String uri, ReqRespObjCapsule reqRespCapsule)
            throws BExceptions, BExceptions {
        if (bServiceNotReady) {
            reqRespCapsule.reqLogger.error("Service is not yet ready to process any request!");
            throw new BExceptions(FwConstants.PCodes.SERVICE_UNAVAILABLE, "Service Not Ready");
        }
        TLSDataCapsule.makeNewThreadLocalData();
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        BExceptions exceptions = new BExceptions();
        Long reqIDLVal = reqID.getAndIncrement();
        String reqIDLValStr = Long.toString(reqIDLVal);
        RequestContextImpl reqCtx = new RequestContextImpl();
        reqCtx.setRequestTS(System.currentTimeMillis());
        reqCtx.setURI(uri);
        reqCtx.setCallerIP(Utilities.getClientIpAddr(reqRespCapsule.reqHeaders));
        reqCtx.setLocale("en_US");
        reqCtx.setRequestID(reqIDLValStr);
        reqCtx.setInHeaders(reqRespCapsule.reqHeaders);
        readCookiesFromHeader(gLogger, reqRespCapsule.reqHeaders, reqCtx);
        reqRespCapsule.reqLogger.info("Request received for endpoint: [" + uri + "]");
        BaseResponse respObj = null;
        SearchResult sr = urlPathProcessor.search(method, uri);
        if (sr == null || sr.varH == null || sr.varH.blModule == null || sr.varH.epd == null) {
            throw new BExceptions(FwConstants.PCodes.REQUEST_NOT_SUPPORTED, "Request Not Supported");
        } else {
            BLModule blMod = sr.varH.blModule;
            String blModId = blMod.getClass().getSimpleName();
            String baName = sr.varH.epd.uri;
            reqRespCapsule.sr = sr;
            BLogger reqLogger = BLoggerFactory.create(blModId, reqIDLVal, baName);
            reqRespCapsule.reqLogger = reqLogger;
            reqLogger.info("Request START -- " + reqCtx);
            tld.extractRequiredData(reqLogger, reqRespCapsule.reqHeaders);
            tld.setCallerReqID(reqLogger, getApplication(), reqIDLValStr);
            try {
                respObj = invokeBLMethod(reqLogger, reqRespCapsule, blModId, blMod, sr, jsonProc, reqCtx, exceptions);
                writeCookiesAndHeaders(reqCtx, reqRespCapsule);
                performRequestAudit(reqCtx, reqRespCapsule, null);
            } catch (BExceptions exs) {
                writeCookiesAndHeaders(reqCtx, reqRespCapsule);
                performRequestAudit(reqCtx, reqRespCapsule, exs);
                throw exs;
            }
        }
        reqRespCapsule.reqLogger.debug("generating output");
        // Map<String, String> respHeaders = new HashMap<String, String>();
        UTF8String respString = null;
        if (respObj != null) {
            BResponseCodes respCodes = new BResponseCodes(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY);
            respObj.setCodes(respCodes.getCodes());
            respString = jsonProc.toJson(respObj);
        } else {
            respString = generateStdResponse(reqRespCapsule.reqLogger, jsonProc, exceptions);
        }
        reqRespCapsule.outputJSON = respString;
    }

    void performRequestAudit(RequestContextImpl reqCtx, ReqRespObjCapsule reqRespCapsule, BExceptions exs) {
        try {
            if (reqRespCapsule.reqAuditor != null) {
                reqRespCapsule.reqAuditor.audit(reqRespCapsule.reqLogger, reqRespCapsule.blModServices,
                        reqRespCapsule.reqDTOObject, reqRespCapsule.respDTOObject, exs);
            }
        } catch (BExceptions e) {
            reqRespCapsule.reqLogger.error("RequestAuditor: threw exception");
            reqRespCapsule.reqLogger.error(e);
        }
    }

    void writeCookiesAndHeaders(RequestContextImpl reqCtx, ReqRespObjCapsule reqRespCapsule) {
        writeCookiesToHeaders(gLogger, reqCtx, reqRespCapsule.respHeaders);
        if (reqCtx.getAllOutHeaders() != null) {
            reqCtx.getAllOutHeaders().forEach((name, value) -> {
                List<String> values = new ArrayList<String>();
                values.add(value);
                reqRespCapsule.respHeaders.put(name, values);
            });
        }
    }

    private BaseResponse invokeBLMethod(BLogger logger, ReqRespObjCapsule reqRespCapsule, String blModId,
                                        BLModule blMod, URLPathProcessor.SearchResult sr, BJson jsonProc, RequestContextImpl reqCtx,
                                        BExceptions exceptions) throws BExceptions {
        Object reqObject = null;
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        BLModServicesImpl blModServices = new BLModServicesImpl(globalServiceCapsule, reqCtx.getRequestID(),
                tld.getCallerReqID());
        MonitoringTracker perfTracker = blModServices.getMonTracker();
        PerfMonData perfMonData = perfTracker.startTracking(MonEvent.MODULE, blModId);
        perfMonData.setReqSize((reqRespCapsule.payLoad == null ? 0 : reqRespCapsule.payLoad.length()));
        reqRespCapsule.perfTracker = perfTracker;
        reqRespCapsule.perfMonData = perfMonData;
        reqRespCapsule.dbm = globalServiceCapsule.getDBMFactory().getDBM();
        reqRespCapsule.blModServices = blModServices;
        logger.debug("reading input - payload, query params and path vars");
        try {
            if (sr.varH.epd.reqType != null && sr.varH.epd.reqType != Object.class) {
                if (reqRespCapsule.payLoad != null && !reqRespCapsule.payLoad.isBlank()) {
                    reqObject = jsonProc.fromJson(reqRespCapsule.payLoad, sr.varH.epd.reqType);
                } else if (sr.varH.epd.reqType == FormDataInput.class) {
                    reqObject = new FormDataInputImpl(reqRespCapsule.formHeaders, reqRespCapsule.formData);
                } else {
                    reqObject = sr.varH.epd.reqType.getConstructor().newInstance();
                }
                reqRespCapsule.reqDTOObject = reqObject;
                QueryNPathPathReader.readFormData(logger, reqObject, reqRespCapsule.formHeaders,
                        reqRespCapsule.formData, exceptions);
                QueryNPathPathReader.readQueryParams(logger, reqObject, reqRespCapsule.qparameters, exceptions);
                QueryNPathPathReader.readPathParams(logger, reqObject, sr.varH.variables, sr.segments, sr.idx,
                        exceptions);
            }
        } catch (Exception e) {
            exceptions.add(new BExceptions(e, FwConstants.PCodes.INVALID_VALUE));
        }
        checkAndThrow(logger, exceptions);
        performAuthz(logger, reqCtx, blModServices, reqRespCapsule.reqHeaders, sr.varH.epd.permission);
        logger.debug("performing input validation");
        CommonValidatorImpl validator = new CommonValidatorImpl();
        AnnotatedFieldValidator.validate(logger, reqObject, validator, exceptions);
        checkAndThrow(logger, exceptions);
        Parameter[] parameters = sr.varH.epd.method.getParameters();
        Object[] params = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(BLogger.class)) {
                params[i] = logger;
            } else if (parameters[i].getType().equals(BLModServices.class)) {
                params[i] = blModServices;
            } else if (parameters[i].getType().equals(RequestContext.class)) {
                params[i] = reqCtx;
            } else if (sr.varH.epd.reqType != null && sr.varH.epd.reqType != Object.class
                    && (parameters[i].getType().equals(sr.varH.epd.reqType))) {
                params[i] = sr.varH.epd.reqType.cast(reqObject);
            } else if (sr.varH.epd.auditType != null && sr.varH.epd.auditType != Object.class
                    && (parameters[i].getType().equals(sr.varH.epd.auditType))) {
                try {
                    Object auditorObj = parameters[i].getType().getConstructor().newInstance();
                    params[i] = sr.varH.epd.auditType.cast(auditorObj);
                    reqRespCapsule.reqAuditor = RequestAuditor.class.cast(auditorObj);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    logger.error(e);
                    exceptions.add(new BExceptions(FwConstants.PCodes.INTERNAL_ERROR,
                            "Internal error due to method invocation!"));
                }
            } else if (sr.varH.epd.pageHandlerType != null && sr.varH.epd.pageHandlerType != Object.class
                    && (parameters[i].getType().equals(sr.varH.epd.pageHandlerType))) {
                try {
                    Object pghObj = parameters[i].getType().getConstructor().newInstance();
                    params[i] = sr.varH.epd.pageHandlerType.cast(pghObj);
                    reqRespCapsule.pageHandler = PageHandler.class.cast(pghObj);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    logger.error(e);
                    exceptions.add(new BExceptions(FwConstants.PCodes.INTERNAL_ERROR,
                            "Internal error due to method invocation!"));
                }
            }
        }
        BaseResponse respObj = null;
        try {
            if (!PaginationHelper.preHandlePagination(logger, blModServices, reqRespCapsule)) {
                respObj = (BaseResponse) sr.varH.epd.method.invoke(blMod, params);
                reqRespCapsule.respDTOObject = respObj;
            }
            PaginationHelper.postHandlePagination(logger, blModServices, reqRespCapsule);
        } catch (IllegalAccessException e) {
            logger.error(e);
            exceptions.add(
                    new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Internal error due to method invocation!"));
        } catch (InvocationTargetException e) {
            Throwable ex = e.getCause();
            if (ex instanceof BExceptions) {
                exceptions.add((BExceptions) ex);
            } else {
                exceptions.add(new BExceptions(e.getCause(), FwConstants.PCodes.INVALID_VALUE));
            }
        }
        checkAndThrow(logger, exceptions);
        return reqRespCapsule.respDTOObject;
    }

    private void performAuthz(BLogger logger, RequestContextImpl reqCtx, BLModServices blModServices,
                              Map<String, List<String>> reqHeaders, String permission) throws BExceptions {
        if (authzModule != null) {
            long timeTaken = System.currentTimeMillis();
            logger.info("START - performAuthz");
            BExceptions err = null;
            TokenDetails td = DummyTokenDetails.dtd;
            try {
                td = authzModule.validateToken(logger, blModServices, reqHeaders);
            } catch (BExceptions ex) {
                err = ex;
            }
            if (permission.equalsIgnoreCase(UniversalConstants.SPECIAL_NO_VALIDATE_PERMISSION)) {
                logger.info("skipping permission check! " + reqCtx.getCallerIP());
            } else {
                if (err != null) {
                    throw err;
                }
                authzModule.validatePermission(logger, blModServices, td, permission);
            }
            reqCtx.setTokenDetails(td);
            timeTaken = System.currentTimeMillis() - timeTaken;
            logger.info("END - performAuthz [" + timeTaken + "]");
        }
    }

    private Function<ContentDisposition, String> getFormParamValue() {
        return x -> x.getParameters().get("filename") == null ? "" : x.getParameters().get("filename");
    }

    private Function<ContentDisposition, String> getFormParamName() {
        return x -> x.getParameters().get("name");
    }

    private boolean containsInterface(Object object, Class<?> interfaceCls) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>(Arrays.asList(object.getClass().getInterfaces()));
        if (interfaces.contains(interfaceCls)) {
            return true;
        }
        return false;
    }
}
