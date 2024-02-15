package com.bundee.msfw.servicefw.blmods;

import java.util.HashSet;
import java.util.Set;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.BLModule;
import com.bundee.msfw.interfaces.endpoint.BEndpoint;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.reqrespi.RequestContext;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringHelper;
import com.bundee.msfw.servicefw.srvutils.monitor.MonitoringTracker;
import com.bundee.msfw.servicefw.srvutils.monitor.SvcMonData;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class HealthCheckBLModule implements BLModule {
	public HealthCheckBLModule() {
	}

	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions, BExceptions {
	}

	@Override
	public boolean authzRequired() { return false; };
	
	@BEndpoint(uri= FwConstants.ENDPOINT_URLS.SVC_HEALTH_DETAILS, httpMethod=UniversalConstants.GET, permission=FwConstants.ENDPOINT_PERMISSIONS.SVC_HEALTH_DETAILS)
	public BaseResponse getServiceHealthlogDetails(BLogger logger, RequestContext reqCtx, BLModServices blModServices) throws BExceptions {
		MonitoringTracker lastMonData = MonitoringHelper.getLastMonTrack();
		if(lastMonData == null) {
			throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "Service monitoring is not enabled!");
		}
		Set<SvcMonData> svcMonDataList = new HashSet<SvcMonData>();
		lastMonData.fillSvcMondata(svcMonDataList);
		return new ServiceMonData(svcMonDataList);
	}
	
	
	private class ServiceMonData extends BaseResponse {
        @SuppressWarnings("unused")
		private Set<SvcMonData> svcMonDataList;
		public ServiceMonData(Set<SvcMonData> svcMonDataList) {
			this.svcMonDataList = svcMonDataList;
		}
	}
}
