package com.bundee.msfw.servicefw.fw;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.CustomService;
import com.bundee.msfw.interfaces.emaili.EmailerService;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.os.ObjectStoreService;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.srvutils.config.CommonFileCfgDefs;
import com.bundee.msfw.servicefw.srvutils.email.EmailerServiceImpl;
import com.bundee.msfw.servicefw.srvutils.location.LocationServiceImpl;
import com.bundee.msfw.servicefw.srvutils.os.ObjectStoreServiceImpl;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import com.bundee.msfw.servicefw.srvutils.utils.ServiceIniter;
import com.bundee.msfw.servicefw.srvutils.vault.VaultLocal;
import com.bundee.msfw.services.location.LocationService;

public class ServiceInitializer {
	private static ServiceInitializer si = new ServiceInitializer();

	private class ServiceDefinition {
		public ServiceDefinition(Class<?> implClass, String interfaceClassName) {
			this.implClass = implClass;
			this.interfaceClassName = interfaceClassName;
		}

		public Class<?> getImplClass() {
			return implClass;
		}

		public String getInterfaceClassName() {
			return interfaceClassName;
		}

		private Class<?> implClass;
		private String interfaceClassName;
	}

	private static final Map<String, ServiceDefinition> className2ClassMap = new TreeMap<String, ServiceDefinition>(
			String.CASE_INSENSITIVE_ORDER);

	public static void init() {
		className2ClassMap.put("ObjectStoreServiceImpl",
				si.new ServiceDefinition(ObjectStoreServiceImpl.class, ObjectStoreService.class.getName()));
		className2ClassMap.put("EmailerServiceImpl",
				si.new ServiceDefinition(EmailerServiceImpl.class, EmailerService.class.getName()));
		className2ClassMap.put("VaultLocal", si.new ServiceDefinition(VaultLocal.class, VaultService.class.getName()));
		className2ClassMap.put("LocationService", si.new ServiceDefinition(LocationServiceImpl.class, LocationService.class.getName()));
	}

	public static void initializeServices(BLogger logger, FileCfgHandler fileCfgHandler,
			GlobalServiceCapsule globalServiceCapsule, BExceptions exceptions) {
		String svcEnabledList = fileCfgHandler.getCfgParamStr(CommonFileCfgDefs.CLS_LOADER_PARAMS.SVC_ENABLED_LIST);

		String[] svcEnabledArray = svcEnabledList == null || svcEnabledList.isBlank() ? null
				: svcEnabledList.split(",");

		Set<String> enabledServices = new HashSet<String>();
		if (svcEnabledArray != null) {
			enabledServices.addAll(Arrays.asList(svcEnabledArray));
		}

		enabledServices.forEach(es -> {
			initializeService(logger, fileCfgHandler, globalServiceCapsule, es, exceptions);
		});
	}

	private static void initializeService(BLogger logger, FileCfgHandler fileCfgHandler,
			GlobalServiceCapsule globalServiceCapsule, final String serviceName, BExceptions exceptions) {
		ServiceDefinition svcClassDef = className2ClassMap.get(serviceName);
		if (svcClassDef == null) {
			exceptions.add(new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Invalid service: " + serviceName));
		} else {
			List<String> svcClassInterfaces = new ArrayList<String>();
			svcClassInterfaces.add(ServiceIniter.class.getName());

			try {
				Object svcObj = searchInterfaces(svcClassDef.getImplClass(), svcClassDef.getInterfaceClassName(), null,
						exceptions);
				if (svcObj != null) {
					ServiceIniter sinit = null;
					if (svcObj != null) {
						globalServiceCapsule.setService(svcObj);
						sinit = (ServiceIniter) svcObj;
						try {
							BLModServicesImpl blModServices = new BLModServicesImpl(globalServiceCapsule);
							sinit.init(logger, fileCfgHandler, globalServiceCapsule.getVaultService(), blModServices);
							logger.info(svcClassDef.getInterfaceClassName() + " initialized successfully");
						} catch (BExceptions e) {
							exceptions.add(e);
						}
					}
				}
			} catch (InvocationTargetException | IllegalAccessException | InstantiationException
					| NoSuchMethodException e) {
				exceptions.add(new BExceptions(e, FwConstants.PCodes.INTERNAL_ERROR));
			}
		}
	}

	public static CustomService initializeCustomService(BLogger logger, final String customSvcClassFQDN,
			BExceptions exceptions) {
		if (customSvcClassFQDN == null || customSvcClassFQDN.isEmpty()) {
			return null;
		}

		List<String> svcClassFQDNs = new ArrayList<String>();
		svcClassFQDNs.add(customSvcClassFQDN);
		Set<Object> modules = new HashSet<Object>();

		loadClasses(svcClassFQDNs, CustomService.class.getName(), null, modules, exceptions);
		if (modules.isEmpty())
			return null;
		return (CustomService) modules.iterator().next();
	}

	public static void initializeCustomServices(BLogger logger, FileCfgHandler fileCfgHandler,
			GlobalServiceCapsule globalServiceCapsule, BExceptions exceptions) {
		BLModServicesImpl blModServices = new BLModServicesImpl(globalServiceCapsule);

		Map<String, Object> allCfgs = fileCfgHandler.getAllCfgParams();
		allCfgs.entrySet().forEach(entry -> {
			if (entry.getKey().startsWith(CommonFileCfgDefs.CLS_LOADER_PARAMS.SVC_CUSTOM_PFX)) {
				String customSvcName = entry.getKey()
						.substring(CommonFileCfgDefs.CLS_LOADER_PARAMS.SVC_CUSTOM_PFX.length());
				String customSvcClassFQDN = (String) (entry.getValue() instanceof String ? entry.getValue() : "");

				CustomService cService = initializeCustomService(logger, customSvcClassFQDN, exceptions);
				if (cService != null) {
					try {
						cService.init(logger, fileCfgHandler, blModServices);

						globalServiceCapsule.addCustomService(customSvcName, cService);
						logger.info(cService.getClass().getName() + " initialized successfully");
					} catch (BExceptions e) {
						exceptions.add(e);
					}
				}
			}
		});
	}

	public static void loadClasses(List<String> classFQDNs, String primaryIName, List<String> interfaces,
			Set<Object> modules, BExceptions exceptions) {
		if (primaryIName == null || primaryIName.isEmpty() || modules == null)
			return;

		Set<String> expectedInterfaces = new HashSet<String>();
		if (interfaces != null) {
			expectedInterfaces.addAll(interfaces);
		}

		for (String classFQDN : classFQDNs) {
			Object ifObj = loadClass(classFQDN, primaryIName, expectedInterfaces, exceptions);
			if (ifObj != null) {
				modules.add(ifObj);
			}
		}
	}

	public static Object loadClass(String classFQDN, String primaryIName, Set<String> expectedInterfaces,
			BExceptions exceptions) {
		Object interfaceObj = null;
		try {
			Class<?> modCls = Class.forName(classFQDN);
			interfaceObj = searchInterfaces(modCls, primaryIName, expectedInterfaces, exceptions);
		} catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException
				| NoSuchMethodException e) {
			exceptions.add(new BExceptions(e, FwConstants.PCodes.INTERNAL_ERROR));
		}
		return interfaceObj;
	}

	private static Object searchInterfaces(Class<?> modCls, String primaryIName, Set<String> expectedInterfaces,
			BExceptions exceptions) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Object interfaceObj = null;
		if (modCls != null) {
			Class<?>[] interfaceObjs = modCls.getInterfaces();
			for (int idx = 0; idx < interfaceObjs.length; idx++) {
				if (primaryIName.equalsIgnoreCase(interfaceObjs[idx].getTypeName())) {
					interfaceObj = modCls.getDeclaredConstructor().newInstance();
				}
				if (expectedInterfaces != null && expectedInterfaces.contains(interfaceObjs[idx].getTypeName())) {
					expectedInterfaces.remove(interfaceObjs[idx].getTypeName());
				}
			}
			if (interfaceObj == null) {
				exceptions.add(new BExceptions(FwConstants.PCodes.INVALID_VALUE,
						modCls.getName() + " does not implement primary interface " + primaryIName));
			}
			if (expectedInterfaces != null && !expectedInterfaces.isEmpty()) {
				exceptions.add(new BExceptions(FwConstants.PCodes.INVALID_VALUE,
						modCls.getName() + " does not implement " + expectedInterfaces));
			}
		}

		return interfaceObj;
	}
}
