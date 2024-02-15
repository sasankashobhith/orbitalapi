package com.bundee.msfw.servicefw.srvutils.cache;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.DCObject;
import com.bundee.msfw.interfaces.utili.dc.DCacheReader;
import com.bundee.msfw.interfaces.utili.dc.DCacheWriter;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class DLocalCacheMgrImpl extends DBaseCacheMgrImpl {
	private Map<Integer, DLocalCacheImpl> tenantCacheMap;
	private final ReadWriteLock readWriteLock;
	
	public DLocalCacheMgrImpl() {
		readWriteLock = new ReentrantReadWriteLock(true); //create a "fair" lock
		tenantCacheMap = new HashMap<Integer, DLocalCacheImpl>();
		DLocalCacheImpl globalCache = new DLocalCacheImpl();
		tenantCacheMap.put(0, globalCache);
	}

	@Override
	public DCacheWriter getGlobalCacheW() throws BExceptions {
		Lock rlck = readWriteLock.readLock();
		rlck.lock();
		DLocalCacheImpl gCache = tenantCacheMap.get(0);
		rlck.unlock();
		return gCache;
	}

	@Override
	public DCacheWriter getTenantCacheW(int tenantID) throws BExceptions {
		DLocalCacheImpl tenantCache = null;
		Lock rlck = readWriteLock.readLock();
		rlck.lock();
		boolean bTenantCacheExists = tenantCacheMap.containsKey(tenantID);
		
		if(!bTenantCacheExists) {
			rlck.unlock();
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			tenantCache = new DLocalCacheImpl();
			tenantCacheMap.put(tenantID, tenantCache);
			wlck.unlock();
		} else {
			tenantCache = tenantCacheMap.get(tenantID);
			rlck.unlock();
		}
		return tenantCache;
	}

	@Override
	public DCacheReader getGlobalCacheR() throws BExceptions {
		DCacheReader tenantCache = null;
		Lock rlck = readWriteLock.readLock();
		rlck.lock();
		tenantCache = tenantCacheMap.get(0);
		rlck.unlock();
		return tenantCache;
	}

	@Override
	public DCacheReader getTenantCacheR(int tenantID) throws BExceptions {
		Lock rlck = readWriteLock.readLock();
		rlck.lock();
		DCacheReader tenantCache = tenantCacheMap.get(tenantID);
		rlck.unlock();
		
		if(tenantCache == null) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Cache object does not exist for tenantID: " + tenantID);
		}
		return tenantCache;
	}
	
	private class DLocalCacheImpl implements DCacheReader, DCacheWriter {
		Map<String, Map<String, Object>> groupStrValuesMap;
		Map<String, Map<Integer, Object>> groupIntValuesMap;
		Map<String, Map<DCObject, Object>> groupObjValuesMap;
		private final ReadWriteLock readWriteLock;
		
		public DLocalCacheImpl() {
			readWriteLock = new ReentrantReadWriteLock(true); //create a "fair" lock
			groupStrValuesMap = new TreeMap<String, Map<String, Object>>(String.CASE_INSENSITIVE_ORDER);
			groupIntValuesMap = new TreeMap<String, Map<Integer, Object>>(String.CASE_INSENSITIVE_ORDER);
			groupObjValuesMap = new TreeMap<String, Map<DCObject, Object>>(String.CASE_INSENSITIVE_ORDER);
		}
		
		@Override
		public void setStrValue(String group, String key, String val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setIntValue(String group, String key, int val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setObjValue(String group, String key, DCObject val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setStrValue(String group, int key, String val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setIntValue(String group, int key, int val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setObjValue(String group, int key, DCObject val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setStrValue(String group, DCObject key, String val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}
		
		@Override
		public void setIntValue(String group, DCObject key, int val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}
		
		@Override
		public void setObjValue(String group, DCObject key, DCObject val) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			setObjValue(group, key, (Object) val);
			wlck.unlock();
		}

		@Override
		public void setObjMapS(String group, Map<String, DCObject> kvMap) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			for(Map.Entry<String, DCObject> pair : kvMap.entrySet()) {
				setObjValue(group, pair.getKey(), pair.getValue());
			}
			wlck.unlock();
		}

		@Override
		public void setObjMapI(String group, Map<Integer, DCObject> kvMap) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			for(Map.Entry<Integer, DCObject> pair : kvMap.entrySet()) {
				setObjValue(group, pair.getKey(), pair.getValue());
			}
			wlck.unlock();
		}

		@Override
		public void setObjMapO(String group, Map<DCObject, DCObject> kvMap) {
			Lock wlck = readWriteLock.writeLock();
			wlck.lock();
			for(Map.Entry<DCObject, DCObject> pair : kvMap.entrySet()) {
				setObjValue(group, pair.getKey(), pair.getValue());
			}
			wlck.unlock();
		}
		
		@Override
		public String getStrValue(String group, String key) {
			Object obj = getStrObjValue(group, key);
			if(obj != null && obj instanceof String) {
				return (String) obj;
			}
			
			return null;
		}

		@Override
		public int getIntValue(String group, String key) {
			Object obj = getStrObjValue(group, key);
			if(obj != null && obj instanceof Integer) {
				return (Integer) obj;
			}
			
			return -1;
		}

		@Override
		public DCObject getObjValue(String group, String key) {
			Object obj = getStrObjValue(group, key);
			if(obj != null && obj instanceof DCObject) {
				DCObject dcObj = (DCObject) obj;
				return dcObj.makeCopy();
			}
			
			return null;
		}

		@Override
		public String getStrValue(String group, int key) {
			Object obj = getIntObjValue(group, key);
			if(obj != null && obj instanceof String) {
				return (String) obj;
			}
			
			return null;
		}

		@Override
		public int getIntValue(String group, int key) {
			Object obj = getIntObjValue(group, key);
			if(obj != null && obj instanceof Integer) {
				return (Integer) obj;
			}
			
			return -1;
		}

		@Override
		public DCObject getObjValue(String group, int key) {
			Object obj = getIntObjValue(group, key);
			if(obj != null && obj instanceof DCObject) {
				DCObject dcObj = (DCObject) obj;
				return dcObj.makeCopy();
			}
			
			return null;
		}

		@Override
		public String getStrValue(String group, DCObject key) {
			Object obj = getObjObjValue(group, key);
			if(obj != null && obj instanceof String) {
				return (String) obj;
			}
			
			return null;
		}

		@Override
		public int getIntValue(String group, DCObject key) {
			Object obj = getObjObjValue(group, key);
			if(obj != null && obj instanceof Integer) {
				return (Integer) obj;
			}
			
			return -1;
		}

		@Override
		public DCObject getObjValue(String group, DCObject key) {
			Object obj = getObjObjValue(group, key);
			if(obj != null && obj instanceof DCObject) {
				DCObject dcObj = (DCObject) obj;
				return dcObj.makeCopy();
			}
			
			return null;
		}
		
		@Override
		public void findAnyObjValueS(String group, Collection<String> keys, Map<String, DCObject> valuesMap) {
			for(String key : keys) {
				DCObject dcObj = getObjValue(group, key);
				if(dcObj != null) {
					valuesMap.put(key, dcObj.makeCopy());
				}
			}
		}

		@Override
		public void findAnyObjValueI(String group, Collection<Integer> keys, Map<Integer, DCObject> valuesMap) {
			for(Integer key : keys) {
				DCObject dcObj = getObjValue(group, key);
				if(dcObj != null) {
					valuesMap.put(key, dcObj.makeCopy());
				}
			}
		}

		@Override
		public void findAnyObjValueO(String group, Collection<DCObject> keys, Map<DCObject, DCObject> valuesMap) {
			for(DCObject key : keys) {
				Object obj = getObjObjValue(group, key);
				if(obj == null || !(obj instanceof DCObject)) continue;
				DCObject dcObj = (DCObject) obj; 
				valuesMap.put(key, dcObj.makeCopy());
			}
		}
		
		@Override
		public Map<String, DCObject> getAllStrObjValues(String group) {
			Map<String, DCObject> kvMap = new TreeMap<String, DCObject>();
			if(groupStrValuesMap.containsKey(group)) {
				Map<String, Object> valuesMap = groupStrValuesMap.get(group);
				for(Map.Entry<String, Object> pair : valuesMap.entrySet()) {
					String key = pair.getKey();
					Object obj = pair.getValue();
					if(obj instanceof DCObject) {
						kvMap.put(key, (DCObject) obj);
					}
				}
			}
			return kvMap;
		}
		
		private void setObjValue(String group, String key, Object val) {
			Map<String, Object> strValuesMap = null;
			if(groupStrValuesMap.containsKey(group)) {
				strValuesMap = groupStrValuesMap.get(group);
			} else {
				strValuesMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
				groupStrValuesMap.put(group, strValuesMap);
			}
			strValuesMap.put(key, val);
		}
		
		
		private void setObjValue(String group, Integer key, Object val) {
			Map<Integer, Object> intValuesMap = null;
			if(groupIntValuesMap.containsKey(group)) {
				intValuesMap = groupIntValuesMap.get(group);
			} else {
				intValuesMap = new HashMap<Integer, Object>();
				groupIntValuesMap.put(group, intValuesMap);
			}
			intValuesMap.put(key, val);
		}

		private void setObjValue(String group, DCObject key, Object val) {
			Map<DCObject, Object> objValuesMap = null;
			if(groupObjValuesMap.containsKey(group)) {
				objValuesMap = groupObjValuesMap.get(group);
			} else {
				objValuesMap = new HashMap<DCObject, Object>();
				groupObjValuesMap.put(group, objValuesMap);
			}
			objValuesMap.put(key, val);
		}

		private Object getStrObjValue(String group, String key) {
			Object obj = null;
			Lock rlck = readWriteLock.writeLock();
			rlck.lock();
			if(groupStrValuesMap.containsKey(group)) {
				obj = groupStrValuesMap.get(group).get(key);
			}
			rlck.unlock();
			return obj;
		}

		private Object getIntObjValue(String group, Integer key) {
			Object obj = null;
			Lock rlck = readWriteLock.writeLock();
			rlck.lock();
			if(groupIntValuesMap.containsKey(group)) {
				obj = groupIntValuesMap.get(group).get(key);
			}
			rlck.unlock();
			return obj;
		}

		private Object getObjObjValue(String group, DCObject key) {
			Object obj = null;
			Lock rlck = readWriteLock.writeLock();
			rlck.lock();
			if(groupObjValuesMap.containsKey(group)) {
				obj = groupObjValuesMap.get(group).get(key);
			}
			rlck.unlock();
			return obj;
		}
	}
}
