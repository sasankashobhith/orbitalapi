package com.bundee.msfw.interfaces.utili.dc;

import java.util.Map;

import com.bundee.msfw.defs.DCObject;

public interface DCacheWriter {
	void setStrValue(String group, String key, String val);
	void setIntValue(String group, String key, int val);
	void setObjValue(String group, String key, DCObject val);

	void setStrValue(String group, int key, String val);
	void setIntValue(String group, int key, int val);
	void setObjValue(String group, int key, DCObject val);

	void setStrValue(String group, DCObject key, String val);
	void setIntValue(String group, DCObject key, int val);
	void setObjValue(String group, DCObject key, DCObject val);
	
	void setObjMapS(String group, Map<String, DCObject> kvMap);
	void setObjMapI(String group, Map<Integer, DCObject> kvMap);
	void setObjMapO(String group, Map<DCObject, DCObject> kvMap);
}
