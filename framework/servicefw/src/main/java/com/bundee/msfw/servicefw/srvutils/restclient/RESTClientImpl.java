package com.bundee.msfw.servicefw.srvutils.restclient;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.fcfgi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.restclienti.*;
import com.bundee.msfw.interfaces.vault.*;
import com.bundee.msfw.servicefw.srvutils.config.*;
import com.bundee.msfw.servicefw.srvutils.utils.*;
import org.apache.http.client.config.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;

public class RESTClientImpl implements RESTClient {
    String configPfx = null;
    String clientID = null;
    private CloseableHttpClient httpClient = null;
    private SSLConnectionSocketFactory sslFactory = null;
    private boolean bSSLEnabled = true;
    private String healthCheckURL = null;

    public RESTClientImpl(BLogger logger, String clientID, String configPfx, FileCfgHandler fch,
                          VaultService vaultService) throws BExceptions {
        this.clientID = clientID;
        this.configPfx = configPfx;
        httpClient = createClient(logger, configPfx, fch, vaultService);
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public void setStandardHealthCheck(String stdHostPort) {
        if (stdHostPort != null && !stdHostPort.isBlank()) {
            String hcURI = FwConstants.ENDPOINT_URLS.SVC_HEALTH_DETAILS;
            if (stdHostPort.endsWith("/")) {
                stdHostPort = stdHostPort.substring(0, stdHostPort.length() - 1);
            }
            healthCheckURL = (bSSLEnabled ? "https://" : "http://") + stdHostPort + (hcURI.startsWith("/") ? hcURI : "/" + hcURI);
        }
    }

    @Override
    public ResponseCapsule sendBytes(BLogger logger, String url, Map<String, String> resqHeaders, String mimeType, byte[] data,
                                     Class<?> respClass, Map<String, String> respHeaders) throws BExceptions {
        RESTHelper restHelper = new RESTHelper(httpClient);
        restHelper.sendBytesData(logger, url, resqHeaders, mimeType, data);
        restHelper.copyResponseHeaders(respHeaders);
        return restHelper.getResponseCapsule(logger, respClass);
    }

    @Override
    public ResponseCapsule receiveBytes(BLogger logger, String method, String url, Map<String, String> reqHeaders,
                                        Object reqObj, Map<String, String> respHeaders) throws BExceptions {
        RESTHelper restHelper = new RESTHelper(httpClient);
        restHelper.sendJSONData(logger, method, url, reqHeaders, reqObj);
        restHelper.copyResponseHeaders(respHeaders);
        return new ResponseCapsuleImpl(restHelper.getResponseBytes());
    }

    @Override
    public ResponseCapsule sendReceiveJSONData(BLogger logger, String method, String url, Map<String, String> reqHeaders,
                                               Object reqObj, Class<?> respClass, Map<String, String> respHeaders) throws BExceptions {
        RESTHelper restHelper = new RESTHelper(httpClient);
        restHelper.sendJSONData(logger, method, url, reqHeaders, reqObj);
        restHelper.copyResponseHeaders(respHeaders);
        return restHelper.getResponseCapsule(logger, respClass);
    }

    @Override
    public Object sendReceive(BLogger logger, String method, String url, Map<String, String> reqHeaders,
                              Object reqObj, Class<?> respClass, Map<String, String> respHeaders) throws BExceptions {
        Object respObj = null;
        ResponseCapsule jrc = sendReceiveJSONData(logger, method, url, reqHeaders, reqObj, respClass, respHeaders);
        if (jrc != null) {
            respObj = jrc.getResponseObject();
        }
        return respObj;
    }

    private CloseableHttpClient createClient(BLogger logger, String configPfx, FileCfgHandler fch,
                                             VaultService vaultService) throws BExceptions {
        int tcpTimeoutSecs = (configPfx == null || configPfx.isBlank()
                ? fch.getCfgParamInt(CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TCP_TIMEOUT_SECS.getValue())
                : fch.getCfgParamInt(
                configPfx + "." + CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TCP_TIMEOUT_SECS.getValue()));
        if (tcpTimeoutSecs < 120)
            tcpTimeoutSecs = 120;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(tcpTimeoutSecs * 2000)
                .setConnectionRequestTimeout(tcpTimeoutSecs * 1000).setSocketTimeout(tcpTimeoutSecs * 1000).build();
        HttpClientBuilder hcBuilder = HttpClients.custom().setDefaultRequestConfig(config);
        String enableSSLStr = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.SSL_ENABLED.getValue(), fch);
        bSSLEnabled = Boolean.parseBoolean(enableSSLStr);
        if (bSSLEnabled) {
            createSSLSocketFactory(logger, configPfx, fch, vaultService);
            hcBuilder = hcBuilder.setSSLSocketFactory(sslFactory);
        }
        hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        return hcBuilder.build();
    }

    private void createSSLSocketFactory(BLogger logger, String configPfx, FileCfgHandler fch,
                                        VaultService vaultService) throws BExceptions {
        String keyStorePath = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.KEYSTORE_PATH.getValue(), fch);
        String keyStoreType = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.KEYSTORE_TYPE.getValue(), fch);
        String keyStorepwdKey = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.KEYSTORE_PWD_KEY.getValue(), fch);
        String trustStorePath = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TRUSTSTORE_PATH.getValue(), fch);
        String trustStoreType = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TRUSTSTORE_TYPE.getValue(), fch);
        String trustStorepwdKey = CommonFileCfgDefs.checkAndGetParam(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.TRUSTSTORE_PWD_KEY.getValue(), fch);
        
        String sslVersion = CommonFileCfgDefs.checkAndGetParamWithDefaults(configPfx,
                CommonFileCfgDefs.GEN_SSL_CFG_PARAMS.SSL_VERSION.getValue(), fch, CommonFileCfgDefs.DEF_CLIENT_PROTOCOL);

        try {
            keyStorePath = fch.getApplication().makeAbsolutePathFromCert(keyStorePath);
            trustStorePath = fch.getApplication().makeAbsolutePathFromCert(trustStorePath);
            
            InputStream ksIS = null;
            InputStream tsIS = null;
            
            if(fch.getApplication().isUsingResource()) {
            	ksIS = resourcePath2IS(keyStorePath);
            	tsIS = resourcePath2IS(trustStorePath);
            } else {
            	ksIS = new FileInputStream(keyStorePath);
            	tsIS = new FileInputStream(trustStorePath);		
            }

            char[] kspwd = vaultService.getValue(logger, keyStorepwdKey).toCharArray();
            char[] tspwd = vaultService.getValue(logger, trustStorepwdKey).toCharArray();
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(ksIS, kspwd);
            keyManagerFactory.init(keyStore, kspwd);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(tsIS, tspwd);
            trustManagerFactory.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance(sslVersion);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    new SecureRandom());

            sslFactory = new SSLConnectionSocketFactory(sslContext, new String[]{sslVersion}, null,
                    new BSSLHostVerifier());
        } catch (Exception e) {
            throw new BExceptions(e, FwConstants.PCodes.REST_CONN_ERROR);
        } finally {
        }
    }

    public SSLConnectionSocketFactory getSSLFactory() throws BExceptions {
        if (!bSSLEnabled)
            throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "SSL is not enabled!");
        if (sslFactory == null) {
            throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "RESTClient not initialized!");
        }
        return sslFactory;
    }

    @Override
    public HealthDetails checkHealth(BLogger logger) {
        HealthDetails hd = new HealthDetails();
        if (healthCheckURL != null) {
            try {
                sendReceiveJSONData(logger, "GET", healthCheckURL, null, null, null, null);
                hd.add(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY, healthCheckURL);
            } catch (BExceptions e) {
                hd.add(e.getCode(), e.getMessage(), healthCheckURL);
            }
        }
        return hd;
    }
    
    private static InputStream resourcePath2IS(String resPath) throws IOException {
    	Resource resource = new ClassPathResource(resPath);
    	if(resource.exists()) {
            return resource.getInputStream();
    	}
    	
    	throw new IOException(resPath + " invalid");
    }
}
