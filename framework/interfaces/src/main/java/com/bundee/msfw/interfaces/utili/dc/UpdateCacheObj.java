package com.bundee.msfw.interfaces.utili.dc;

import com.bundee.msfw.defs.UTF8String;

public interface UpdateCacheObj {
	public enum STATUS {
		INITIAL(1),
		COMPLETED_WITH_SUCCESS(2),
		
		IN_PROGRESS(101),
		
		UNKNOWN(0);
		private int value;
		private static final STATUS[] vals = STATUS.values(); 

		private STATUS(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
		public static STATUS get(int value){
	        for (STATUS e:STATUS.vals) {
	            if(e.getValue() == value)
	                return e;
	        }
	        return STATUS.UNKNOWN;
	    }
	}

	STATUS getStatus();
	public String getBusinessModID();
	public int getUCId();
	public int getTargetTenantId();
	public UTF8String getUCData();
	public long getCreationTS();
	public long getPickupTS();
	public long getCompletionTS();
}
