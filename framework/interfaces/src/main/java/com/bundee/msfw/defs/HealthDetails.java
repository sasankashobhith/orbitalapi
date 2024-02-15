package com.bundee.msfw.defs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HealthDetails {
	public class SingleEdpointHealth {
		private ProcessingCode code = ProcessingCode.UNKNOWN_PC;
		private String errDetail = "";
		private String externalHost = "";
		
		public SingleEdpointHealth(ProcessingCode code, String errDetail, String externalHost) {
			init(code, errDetail, externalHost);
		}

		public ProcessingCode getCode() {
			return code;
		}
		
		public String getErrDetail() {
			return errDetail;
		}
		
		public String getExternalHost() {
			return externalHost;
		}	
		
		private void init(ProcessingCode code, String errDetail, String externalHost) {
			this.code = code;
			this.errDetail = errDetail;
			this.externalHost = externalHost;
		}
	}

	private List<SingleEdpointHealth> endpointHealthDetails;
	
	public void add(ProcessingCode code, String errDetail, String externalHost) {
		SingleEdpointHealth seh = new SingleEdpointHealth(code, errDetail, externalHost);
		endpointHealthDetails.add(seh);
	}

	public Collection<SingleEdpointHealth> getAllEndpointHealthDetails() {
		return endpointHealthDetails;
	}
	
	public HealthDetails() {
		endpointHealthDetails = new ArrayList<SingleEdpointHealth>();
	}
}
