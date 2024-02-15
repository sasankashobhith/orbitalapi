package com.bundee.msfw.interfaces.utili.dc;

import java.util.Collection;
import java.util.Map;

import com.bundee.msfw.defs.DCObject;

public interface DCacheReader {
	String getStrValue(String group, String key);
	int getIntValue(String group, String key);
	DCObject getObjValue(String group, String key);

	String getStrValue(String group, int key);
	int getIntValue(String group, int key);
	DCObject getObjValue(String group, int key);

	String getStrValue(String group, DCObject key);
	int getIntValue(String group, DCObject key);
	DCObject getObjValue(String group, DCObject key);

	Map<String, DCObject> getAllStrObjValues(String group);
	
	void findAnyObjValueS(String group, Collection<String> keys, Map<String, DCObject> valuesMap);
	void findAnyObjValueI(String group, Collection<Integer> keys, Map<Integer, DCObject> valuesMap);
	void findAnyObjValueO(String group, Collection<DCObject> keys, Map<DCObject, DCObject> valuesMap);
}
