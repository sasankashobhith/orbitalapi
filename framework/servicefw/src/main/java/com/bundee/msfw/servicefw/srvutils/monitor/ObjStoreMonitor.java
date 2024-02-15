package com.bundee.msfw.servicefw.srvutils.monitor;

import java.io.InputStream;
import java.util.List;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.os.ObjectStoreService;

public class ObjStoreMonitor implements ObjectStoreService {
	ObjectStoreService osService;
	MonitoringTracker perfTracker;
	
	public ObjStoreMonitor(ObjectStoreService osService, MonitoringTracker perfTracker) {
		this.osService = osService;
		this.perfTracker = perfTracker;
	}
	
	@Override
	public void createObject(BLogger BLogger, String component, String key, byte[] content,
			boolean isInboxOperation) throws BExceptions {
		PerfMonData perfData = perfTracker.startTracking(MonEvent.OBJS, osService.getClass().getSimpleName() + "::createObject");
		try {
			osService.createObject(BLogger, component, key, content, isInboxOperation);
			perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			perfTracker.endTracking(perfData, ex);
			throw ex;
		}
	}

	@Override
	public List<String> listObjects(BLogger logger, String component, boolean isInboxOperation) throws BExceptions {
		PerfMonData perfData = perfTracker.startTracking(MonEvent.OBJS, osService.getClass().getSimpleName() + "::listObjects");
		List<String> val = null;
		try {
			val = osService.listObjects(logger, component, isInboxOperation);
			perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			perfTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}

	@Override
	public void deleteObject(BLogger logger, String component, String key, boolean isInboxOperation)
			throws BExceptions {
		PerfMonData perfData = perfTracker.startTracking(MonEvent.OBJS, osService.getClass().getSimpleName() + "::deleteObject");
		try {
			osService.deleteObject(logger, component, key, isInboxOperation);
			perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			perfTracker.endTracking(perfData, ex);
			throw ex;
		}
	}

	@Override
	public InputStream readObject(BLogger logger, String component, String key, boolean isInboxOperation)
			throws BExceptions {
		PerfMonData perfData = perfTracker.startTracking(MonEvent.OBJS, osService.getClass().getSimpleName() + "::readObject");
		InputStream val = null;
		try {
			val = osService.readObject(logger, component, key, isInboxOperation);
			perfTracker.endTracking(perfData);
		} catch (BExceptions ex) {
			perfTracker.endTracking(perfData, ex);
			throw ex;
		}
		return val;
	}
	
	@Override
	public HealthDetails checkHealth(BLogger logger) {
		return osService.checkHealth(logger);
	}
}
