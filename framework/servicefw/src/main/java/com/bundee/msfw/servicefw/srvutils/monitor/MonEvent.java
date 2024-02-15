package com.bundee.msfw.servicefw.srvutils.monitor;



public enum MonEvent {
	//Basic events - start from 1-499 
    MODULE(1),
    JOB(2),
	
	
	//Service events - start from 500-999
    DB(500),
    CUSTOM_SVC(501),
    VAULT(502),
    OBJS(503),
    EMAILER(504),
    REST_CLIENT(505),
	
    UNKNOWN(0);
    final int value;

	MonEvent(int i) {
        value = i;
    }

	public int getValue() {
		return value;
	}
}
