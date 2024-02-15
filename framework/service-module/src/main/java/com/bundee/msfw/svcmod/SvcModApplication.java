package com.bundee.msfw.svcmod;


import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.bundee.msfw.servicefw.fw.ServiceFramework;

@SpringBootApplication(exclude= {SecurityAutoConfiguration.class})
public class SvcModApplication {
	private static ServiceFramework svcFw = null;
	private static SpringApplication sap = null;
	public static ConfigurableApplicationContext ctx = null;
	
	//Example args: -root <ROOT_PATH> [-certs <CERTS_OVER_RIDE_PATH]
	public static void main(String[] args) {
		svcFw = ServiceFramework.getInstance();
		Map<String, Object> allCfgParams = svcFw.init(args);
		
		sap = new SpringApplication(SvcModApplication.class);
		sap.setDefaultProperties(allCfgParams);
		ctx = sap.run(args);
	}
}

