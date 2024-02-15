package com.bundee.msfw.servicefw.srvutils.config;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class CommonFileCfgDefs {
	public static String SSL_CLIENT_AUTH_WANT = "want";
	public static String SSL_CLIENT_AUTH_NEED = "need";
	
	public static String DEF_SSL_PROTOCOL = "TLS";
	public static String DEF_PROTOCOLS = "TLSv1.2+TLSv1.3";
	public static String DEF_TLS_CIPHERS = "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_CCM,TLS_ECDHE_ECDSA_WITH_AES_128_CCM,TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_CCM_SHA256";
	
	public static String DEF_CLIENT_PROTOCOL = "TLSv1.2";

	public enum GEN_SSL_CFG_PARAMS {
		SSL_ENABLED("enableSSL"), KEYSTORE_PATH("keyStorePath"), KEYSTORE_TYPE("keyStoreType"), KEYSTORE_PWD_KEY("keyStorePwdKey"),
		TRUSTSTORE_PATH("trustStorePath"), TRUSTSTORE_TYPE("trustStoreType"), TRUSTSTORE_PWD_KEY("trustStorePwdKey"),
		TCP_TIMEOUT_SECS("tcpTimeoutSecs"), SSL_PROTOCOL("sslProtocol"), SSL_VERSION("sslVersion"), 
		SSL_CIPHER_SUITE("sslCipherSuite"), SSL_ENABLE_CLIENT_AUTH("enableClientAuth");

		String value;

		GEN_SSL_CFG_PARAMS(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
	
	public enum GEN_CFG_PARAMS {
		SRV_NAME("server.svc-name"),
		SRV_PORT("server.port"),
		SRV_CFG_PFX("server.cfgPfx"),
		SRV_CFG_ENABLE_SSL_TRACE("server.enableSSLTrace"),
	
		MON_ENABLE_PERF_MON("mon.EnablePerfMonitoring"),

		JOB_INITIAL_DELAY_SECS("jobconfig.initialDelaySecs"),
		
		MON_ENABLE_SVC_MON("mon.EnableServiceMonitoring"),
		JOB_SVC_MON_JOB_FREQ("jobconfig.svcMonJobFreq"),
		JOB_SVC_MON_NOTIFICATION_FREQ("jobconfig.svcMonNotificationFreq"),
		JOB_SVC_MON_NOTIFICATION_ON_FAILURE_ONLY("jobconfig.svcMonNotificationOnFailureOnly"),
		JOB_SVC_MON_NOTIFICATION_TO_LIST("jobconfig.svcMonNotificationTOList"),
		JOB_SVC_MON_NOTIFICATION_CC_LIST("jobconfig.svcMonNotificationCCList"),
		
		JOB_DISTRIBUTOR_CLS("jobconfig.DistributorClass"),
		
		MON_ENABLE_CERT_MON("mon.EnableCertMonitoring"),
		JOB_CERT_MON_FREQ("jobconfig.certMonitoringFreq"),
		JOB_CERT_MON_EXPIRY_NOTICE_PERIOD("jobconfig.certExpiryNoticePeriod"),
		JOB_CERT_MON_REMINDER_FREQ("jobconfig.certMonReminderFreq"),
		JOB_CERT_MON_NOTIFICATION_TO_LIST("jobconfig.certMonNotificationTOList"),
		JOB_CERT_MON_NOTIFICATION_CC_LIST("jobconfig.certMonNotificationCCList"),

		SHUTDOWN_INITIAL_DELAY_SECS("shutdownDelaySecs"),
		;

		String value;

		GEN_CFG_PARAMS(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
	
	public enum SB_CFG_PARAMS {
		SSL_ENABLED("server.ssl.enabled"), SRV_PORT("server.port"), APP_NAME("spring.application.name"),
		KS_TYPE("server.ssl.key-store-type"), KS_PATH("server.ssl.key-store"), KS_PWD("server.ssl.key-store-password"),
		SSL_PROTOCOL("server.ssl.protocol"), TLS_PROTOCOLS("server.ssl.enabled-protocols"), 
		TLS_CIPHERS("server.ssl.ciphers"), CLIENT_AUTH("server.ssl.client-auth"), 
		SSL_TRUST_STORE("server.ssl.trust-store"), TS_TYPE("server.ssl.trust-store-type"), SSL_TRUST_STORE_PWD("server.ssl.trust-store-password");

		String value;

		SB_CFG_PARAMS(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}


	public static final class CLS_LOADER_PARAMS {
		public static final String BL_MODULES_OLD = "bl.modules";
		public static final String BL_MODULES_PFX_NEW = "bl.modules.";
		public static final String JOB_MODULES = "job.modules";
		
		public static final String SVC_ENABLED_LIST = "services.enabled-list";

		public static final String SVC_CLASS_VAULT = "service.vault";
		public static final String SVC_CLASS_VSFU = "service.vsfu";
		public static final String SVC_CLASS_VEES = "service.vees";
		public static final String SVC_CLASS_SDS = "service.sds";
		public static final String SVC_CLASS_OS = "service.os";
		public static final String SVC_CLASS_GMR = "service.gmr";
		public static final String SVC_CLASS_EMAIL = "service.email";

		public static final String SVC_CUSTOM_PFX = "service.custom.";
	}

	public static String checkAndGetParam(String configPfx, String configPName, FileCfgHandler fch)
			throws BExceptions {
		String cfgName = (configPfx == null || configPfx.isBlank() ? configPName : configPfx + "." + configPName);
		String value = fch.getCfgParamStr(cfgName);
		if (value == null || value.isBlank()) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, cfgName + " is not provided in the configuration");
		}
		return value;
	}

	public static String checkAndGetParamWithDefaults(String configPfx, String configPName, FileCfgHandler fch, String defvalue) {
		String value = defvalue;
		try {
			value = checkAndGetParam(configPfx, configPName, fch);
		} catch (BExceptions e) {
		}
		return value;
	}
	
	public static String checkAndGetOptParam(String configPfx, String configPName, FileCfgHandler fch)
			throws BExceptions {
		String cfgName = (configPfx == null || configPfx.isBlank() ? configPName : configPfx + "." + configPName);
		return fch.getCfgParamStr(cfgName);
	}
}
