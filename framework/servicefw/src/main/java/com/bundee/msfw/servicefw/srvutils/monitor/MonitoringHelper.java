package com.bundee.msfw.servicefw.srvutils.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.defs.HealthDetails.SingleEdpointHealth;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.CustomService;
import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.BLModServicesImpl;
import com.bundee.msfw.servicefw.srvutils.restclient.RESTClientRuntimeFactoryImpl;

public class MonitoringHelper {
	private static MonitoringTracker lastTrack = null;
	private static final ReadWriteLock monRWLock = new ReentrantReadWriteLock();
	
	Set<HealthCheck> modules;
	
	public MonitoringHelper(Set<HealthCheck> modules) {
		this.modules = modules; 
	}
	
	public void performMonitoring(BLogger logger, BLModServices blModServices) throws BExceptions {
		BLModServicesImpl blModServicesImpl = (BLModServicesImpl) blModServices;
		List<SvcMonData> svcMonDataList = new ArrayList<SvcMonData>();
		for (MonEvent monEvt : MonEvent.values()) {
			getServiceAndCheckHealth(logger, blModServicesImpl.getMonTracker().getEventID(), monEvt, blModServicesImpl, svcMonDataList);
		}
		
		MonitoringTracker monTracker = blModServicesImpl.getMonTracker();
		svcMonDataList.forEach( smd -> monTracker.addSvcMonData(smd));
	}

	private void getServiceAndCheckHealth(BLogger logger, String eventID, MonEvent monEvt,
			BLModServicesImpl monBLModServices, List<SvcMonData> svcMonDataList) {
		try {
			switch (monEvt) {
			case DB:
				callHC(logger, eventID, monEvt, monBLModServices.getDBManager(), "Database", svcMonDataList);
				break;
			case MODULE:
				if (modules != null && !modules.isEmpty()) {
					modules.forEach( hcm -> {
						callHC(logger, eventID, monEvt, hcm, null, svcMonDataList);
					});
				}
				break;
			case CUSTOM_SVC:
				Map<String, CustomService> customServices = monBLModServices.getAllCustomServices();
				customServices.forEach( (name, obj) -> {
					callHC(logger, eventID, monEvt, obj, name, svcMonDataList);
				});
				break;
			case VAULT:
				break;
			case OBJS:
				callHC(logger, eventID, monEvt, monBLModServices.getObjectStoreService(), "ObjectStoreService", svcMonDataList);
				break;
			case EMAILER:
				callHC(logger, eventID, monEvt, monBLModServices.getEmailerService(), "EmailerService", svcMonDataList);
				break;
			case REST_CLIENT:
				RESTClientRuntimeFactoryImpl restF =  (RESTClientRuntimeFactoryImpl)monBLModServices.getRESTClientFactory();
				restF.getAllRESTClients().forEach( (name, rc) -> {
					callHC(logger, eventID, monEvt, rc, name, svcMonDataList);
				});
				break;
			default:
				break;
			}
		} catch (BExceptions e) {
		}
	}

	private void callHC(BLogger logger, String eventID, MonEvent monEvt, HealthCheck hcm,
			String svcName, List<SvcMonData> svcMonDataList) {
		HealthDetails hd = hcm.checkHealth(logger);
		if (hd != null) {
			Collection<SingleEdpointHealth> allHDs = hd.getAllEndpointHealthDetails();
			if (allHDs != null && !allHDs.isEmpty()) {
				allHDs.forEach(seh -> {
					SvcMonData svcmd = new SvcMonData(eventID, monEvt, (svcName != null ? svcName : hcm.getClass().getSimpleName()));
					svcmd.setErrCode(seh.getCode().getCode());
					svcmd.setErrDetail(seh.getErrDetail());
					svcmd.setExtHost(seh.getExternalHost());
					svcMonDataList.add(svcmd);
				});
			}
		}
	}
	
	public static void addTrack(MonitoringTracker monTrack) {
		Lock wl = monRWLock.writeLock();
		wl.lock();
		lastTrack = monTrack;
		wl.unlock();
	}

	public static MonitoringTracker getLastMonTrack() {
		Lock rl = monRWLock.readLock();
		rl.lock();
		MonitoringTracker monTrack = lastTrack;
		rl.unlock();
		return monTrack;
	}
}
