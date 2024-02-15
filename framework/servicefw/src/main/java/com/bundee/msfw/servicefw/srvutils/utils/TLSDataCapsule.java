package com.bundee.msfw.servicefw.srvutils.utils;

public class TLSDataCapsule {
	private static ThreadLocal<ThreadLocalData> threadLocalData = new ThreadLocal<ThreadLocalData>();

	public static ThreadLocalData getCurrentThreadLocalData() {
		return threadLocalData.get();
	}

	public static void makeNewThreadLocalData() {
		threadLocalData.set(new ThreadLocalData());
	}
}
